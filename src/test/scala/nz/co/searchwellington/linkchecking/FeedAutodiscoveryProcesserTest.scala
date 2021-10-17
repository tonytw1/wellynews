package nz.co.searchwellington.linkchecking

import java.util.UUID

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.commentfeeds.CommentFeedDetectorService
import nz.co.searchwellington.htmlparsing.RssLinkExtractor
import nz.co.searchwellington.model.{DiscoveredFeed, Feed, Newsitem}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Matchers.any
import org.mockito.Mockito.{mock, never, verify, when}
import org.mockito.{ArgumentCaptor, Matchers}
import reactivemongo.api.commands.WriteResult

import scala.concurrent.{Await, ExecutionContext, Future}

class FeedAutodiscoveryProcesserTest extends ReasonableWaits {

  private val UNSEEN_FEED_URL = "http://something/new"
  private val EXISTING_FEED_URL = "http://something/old"
  private val RELATIVE_FEED_URL = "/feed.xml"

  private val mongoRepository = mock(classOf[MongoRepository])
  private val rssLinkExtractor = mock(classOf[RssLinkExtractor])
  private val commentFeedDetector = mock(classOf[CommentFeedDetectorService])

  private val resource = Newsitem(id = UUID.randomUUID().toString, page = "https://localhost/test")
  private val pageContent = "Meh"

  private val feedAutodiscoveryProcesser = new FeedAutodiscoveryProcesser(mongoRepository, rssLinkExtractor, commentFeedDetector)

  private val successfulWrite: WriteResult = mock(classOf[WriteResult])
  when(successfulWrite.writeErrors).thenReturn(Seq.empty)

  @Test
  def newlyDiscoveredFeedsUrlsShouldBeRecordedAsDiscoveredFeeds(): Unit = {
    implicit val ec = ExecutionContext.Implicits.global
    val now = DateTime.now

    when(rssLinkExtractor.extractFeedLinks(pageContent)).thenReturn(Seq(UNSEEN_FEED_URL))

    when(commentFeedDetector.isCommentFeedUrl(UNSEEN_FEED_URL)).thenReturn(false)
    when(mongoRepository.getDiscoveredFeedByUrlAndReference(UNSEEN_FEED_URL, resource.page)).thenReturn(Future.successful(None))
    when(mongoRepository.getFeedByUrl(UNSEEN_FEED_URL)).thenReturn(Future.successful(None))
    when(mongoRepository.saveDiscoveredFeed(Matchers.any(classOf[DiscoveredFeed]))(Matchers.eq(ec))).thenReturn(Future.successful(successfulWrite))

    val saved = ArgumentCaptor.forClass(classOf[DiscoveredFeed])

    val eventualBoolean = feedAutodiscoveryProcesser.process(resource, Some(pageContent), now)(ec)
    Await.result(eventualBoolean, TenSeconds)

    verify(mongoRepository).saveDiscoveredFeed(saved.capture())(Matchers.eq(ec))
    assertEquals(UNSEEN_FEED_URL, saved.getValue.url)
    assertEquals(resource.page, saved.getValue.referencedFrom)
  }

  @Test
  def relativeFeedUrlsShouldBeExpandedIntoFullyQualifiedUrls(): Unit = {
    implicit val ec = ExecutionContext.Implicits.global

    when(rssLinkExtractor.extractFeedLinks(pageContent)).thenReturn(Seq(RELATIVE_FEED_URL))

    when(commentFeedDetector.isCommentFeedUrl("https://localhost/feed.xml")).thenReturn(false)
    when(mongoRepository.getDiscoveredFeedByUrlAndReference("https://localhost/feed.xml", resource.page)).thenReturn(Future.successful(None))
    when(mongoRepository.getFeedByUrl("https://localhost/feed.xml")).thenReturn(Future.successful(None))
    when(mongoRepository.saveDiscoveredFeed(Matchers.any(classOf[DiscoveredFeed]))(Matchers.eq(ec))).thenReturn(Future.successful(successfulWrite))

    val saved = ArgumentCaptor.forClass(classOf[DiscoveredFeed])

    Await.result(feedAutodiscoveryProcesser.process(resource, Some(pageContent), DateTime.now), TenSeconds)

    verify(mongoRepository).saveDiscoveredFeed(saved.capture())(Matchers.eq(ec))
    assertEquals("https://localhost/feed.xml", saved.getValue.url)
  }

  @Test
  def doNotRecordDiscoveredFeedsIfWeAlreadyHaveThisFeed(): Unit = {
    implicit val ec = ExecutionContext.Implicits.global

    val autoDiscoveredLinks = Seq(EXISTING_FEED_URL)
    when(rssLinkExtractor.extractFeedLinks(pageContent)).thenReturn(autoDiscoveredLinks)
    when(commentFeedDetector.isCommentFeedUrl(EXISTING_FEED_URL)).thenReturn(false)
    when(mongoRepository.getDiscoveredFeedByUrlAndReference(EXISTING_FEED_URL, resource.page)).thenReturn(Future.successful(None))
    when(mongoRepository.getFeedByUrl(EXISTING_FEED_URL)).thenReturn(Future.successful(Some(mock(classOf[Feed]))))

    Await.result(feedAutodiscoveryProcesser.process(resource, Some(pageContent), DateTime.now), TenSeconds)

    verify(mongoRepository, never).saveDiscoveredFeed(any(classOf[DiscoveredFeed]))(Matchers.eq(ec))
  }

}
