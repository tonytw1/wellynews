package nz.co.searchwellington.linkchecking.processors

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.commentfeeds.CommentFeedDetectorService
import nz.co.searchwellington.htmlparsing.RssLinkExtractor
import nz.co.searchwellington.model.{DiscoveredFeed, Feed, Newsitem, Website}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, never, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import reactivemongo.api.commands.WriteResult

import java.net.URL
import java.util.UUID
import scala.concurrent.{Await, ExecutionContext, Future}

class FeedAutodiscoveryProcessorTest extends ReasonableWaits {

  private val UNSEEN_FEED_URL = new URL("http://something/new")
  private val UNSEEN_FEED_URL_HTTPS = "https://something/new"
  private val EXISTING_FEED_URL = new URL("http://something/old")
  private val EXISTING_FEED_URL_HTTPS = "https://something/old"
  private val RELATIVE_FEED_URL = "/feed.xml"

  private val mongoRepository = mock(classOf[MongoRepository])
  private val rssLinkExtractor = mock(classOf[RssLinkExtractor])
  private val commentFeedDetector = mock(classOf[CommentFeedDetectorService])

  private val resource = Newsitem(id = UUID.randomUUID().toString, page = "https://localhost/test")
  private val pageContent = "Meh"

  private val feedAutodiscoveryProcessor = new FeedAutodiscoveryProcessor(mongoRepository, rssLinkExtractor, commentFeedDetector)

  private val successfulWrite = mock(classOf[WriteResult])
  when(successfulWrite.writeErrors).thenReturn(Seq.empty)

  @Test
  def newlyDiscoveredFeedsUrlsShouldBeRecordedAsDiscoveredFeeds(): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
    val now = DateTime.now
    val source = Website()

    when(rssLinkExtractor.extractFeedLinks(pageContent)).thenReturn(Seq(UNSEEN_FEED_URL.toExternalForm))
    when(commentFeedDetector.isCommentFeedUrl(UNSEEN_FEED_URL, source)).thenReturn(false)
    when(mongoRepository.getDiscoveredFeedByUrl(UNSEEN_FEED_URL.toExternalForm)).thenReturn(Future.successful(None))

    when(mongoRepository.getFeedByUrl(UNSEEN_FEED_URL.toExternalForm)).thenReturn(Future.successful(None))
    when(mongoRepository.getFeedByUrl(UNSEEN_FEED_URL_HTTPS)).thenReturn(Future.successful(None))

    when(mongoRepository.saveDiscoveredFeed(any(classOf[DiscoveredFeed]))(ArgumentMatchers.eq(ec))).thenReturn(Future.successful(successfulWrite))

    val saved: ArgumentCaptor[DiscoveredFeed] = ArgumentCaptor.forClass(classOf[DiscoveredFeed])

    val eventualBoolean = feedAutodiscoveryProcessor.process(resource, Some(pageContent), now)(ec)
    Await.result(eventualBoolean, TenSeconds)

    verify(mongoRepository).saveDiscoveredFeed(saved.capture())(ArgumentMatchers.eq(ec))
    assertEquals(UNSEEN_FEED_URL.toExternalForm, saved.getValue.url)
    assertEquals(resource.page, saved.getValue.occurrences.head.referencedFrom)
  }

  @Test
  def relativeFeedUrlsShouldBeExpandedIntoFullyQualifiedUrls(): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

    val source = Website()
    when(rssLinkExtractor.extractFeedLinks(pageContent)).thenReturn(Seq(RELATIVE_FEED_URL))
    when(commentFeedDetector.isCommentFeedUrl(new URL("https://localhost/feed.xml"), source)).thenReturn(false)
    when(mongoRepository.getDiscoveredFeedByUrl("https://localhost/feed.xml")).thenReturn(Future.successful(None))
    when(mongoRepository.getFeedByUrl("http://localhost/feed.xml")).thenReturn(Future.successful(None))
    when(mongoRepository.getFeedByUrl("https://localhost/feed.xml")).thenReturn(Future.successful(None))
    when(mongoRepository.saveDiscoveredFeed(any(classOf[DiscoveredFeed]))(ArgumentMatchers.eq(ec))).thenReturn(Future.successful(successfulWrite))

    val saved: ArgumentCaptor[DiscoveredFeed] = ArgumentCaptor.forClass(classOf[DiscoveredFeed])

    Await.result(feedAutodiscoveryProcessor.process(resource, Some(pageContent), DateTime.now), TenSeconds)

    verify(mongoRepository).saveDiscoveredFeed(saved.capture())(ArgumentMatchers.eq(ec))
    assertEquals("https://localhost/feed.xml", saved.getValue.url)
  }

  @Test
  def doNotRecordDiscoveredFeedsIfWeAlreadyHaveThisFeed(): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

    val source = Website()
    val autoDiscoveredLinks = Seq(EXISTING_FEED_URL.toExternalForm)
    when(rssLinkExtractor.extractFeedLinks(pageContent)).thenReturn(autoDiscoveredLinks)
    when(commentFeedDetector.isCommentFeedUrl(EXISTING_FEED_URL, source)).thenReturn(false)
    when(mongoRepository.getDiscoveredFeedByUrl(EXISTING_FEED_URL.toExternalForm)).thenReturn(Future.successful(None))
    when(mongoRepository.getFeedByUrl(EXISTING_FEED_URL.toExternalForm)).thenReturn(Future.successful(Some(mock(classOf[Feed]))))
    when(mongoRepository.getFeedByUrl(EXISTING_FEED_URL_HTTPS)).thenReturn(Future.successful(None))

    Await.result(feedAutodiscoveryProcessor.process(resource, Some(pageContent), DateTime.now), TenSeconds)

    verify(mongoRepository, never).saveDiscoveredFeed(any(classOf[DiscoveredFeed]))(ArgumentMatchers.eq(ec))
  }

  @Test
  def shouldIgnoreUnparsableDiscoveredUrls(): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

    when(rssLinkExtractor.extractFeedLinks(pageContent)).thenReturn(Seq("not a url"))

    Await.result(feedAutodiscoveryProcessor.process(resource, Some(pageContent), DateTime.now), TenSeconds)

    verify(mongoRepository, never).saveDiscoveredFeed(any(classOf[DiscoveredFeed]))(ArgumentMatchers.eq(ec))
  }

}
