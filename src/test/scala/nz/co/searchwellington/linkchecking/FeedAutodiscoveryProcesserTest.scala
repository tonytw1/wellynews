package nz.co.searchwellington.linkchecking

import java.util.UUID

import nz.co.searchwellington.commentfeeds.{CommentFeedDetectorService, CommentFeedGuesserService}
import nz.co.searchwellington.htmlparsing.RssLinkExtractor
import nz.co.searchwellington.model.{DiscoveredFeed, Feed, Newsitem}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{mock, never, verify, when}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class FeedAutodiscoveryProcesserTest {

  private val UNSEEN_FEED_URL = "http://something/new"
  private val EXISTING_FEED_URL = "http://something/old"
  private val RELATIVE_FEED_URL = "/feed.xml"

  private val mongoRepository = mock(classOf[MongoRepository])
  private val rssLinkExtractor = mock(classOf[RssLinkExtractor])
  private val commentFeedDetector = mock(classOf[CommentFeedDetectorService])
  private val commentFeedGuesser = mock(classOf[CommentFeedGuesserService])

  private val resource = Newsitem(id = UUID.randomUUID().toString, page = "https://localhost/test")
  private val pageContent = "Meh"
  private var feedAutodiscoveryProcesser = new FeedAutodiscoveryProcesser(mongoRepository, rssLinkExtractor, commentFeedDetector, commentFeedGuesser)

  @Test def newlyDiscoveredFeedsUrlsShouldBeRecordedAsDiscoveredFeeds(): Unit = {
    when(rssLinkExtractor.extractLinks(pageContent)).thenReturn(Seq(UNSEEN_FEED_URL))

    when(commentFeedDetector.isCommentFeedUrl(UNSEEN_FEED_URL)).thenReturn(false)
    when(mongoRepository.getDiscoveredFeedByUrlAndReference(UNSEEN_FEED_URL, resource.page)).thenReturn(Future.successful(None))
    when(mongoRepository.getFeedByUrl(UNSEEN_FEED_URL)).thenReturn(Future.successful(None))

    val saved = ArgumentCaptor.forClass(classOf[DiscoveredFeed])

    feedAutodiscoveryProcesser.process(resource, pageContent, DateTime.now)

    verify(mongoRepository).saveDiscoveredFeed(saved.capture())
    assertEquals(UNSEEN_FEED_URL, saved.getValue.url)
    assertEquals(resource.page, saved.getValue.referencedFrom)
  }

  @Test def relativeFeedUrlsShouldBeExpandedIntoFullyQualifiedUrls(): Unit = {
    when(rssLinkExtractor.extractLinks(pageContent)).thenReturn(Seq(RELATIVE_FEED_URL))

    when(commentFeedDetector.isCommentFeedUrl("https://localhost/feed.xml")).thenReturn(false)
    when(mongoRepository.getDiscoveredFeedByUrlAndReference("https://localhost/feed.xml", resource.page)).thenReturn(Future.successful(None))
    when(mongoRepository.getFeedByUrl("https://localhost/feed.xml")).thenReturn(Future.successful(None))

    val saved = ArgumentCaptor.forClass(classOf[DiscoveredFeed])

    feedAutodiscoveryProcesser.process(resource, pageContent, DateTime.now)

    verify(mongoRepository).saveDiscoveredFeed(saved.capture())
    assertEquals("https://localhost/feed.xml", saved.getValue.url)
  }

  @Test
  def doNotRecordDiscoveredFeedsIfWeAlreadyHaveThisFeed(): Unit = {
    val autoDiscoveredLinks = Seq(EXISTING_FEED_URL)
    when(rssLinkExtractor.extractLinks(pageContent)).thenReturn(autoDiscoveredLinks)
    when(commentFeedDetector.isCommentFeedUrl(EXISTING_FEED_URL)).thenReturn(false)
    when(mongoRepository.getDiscoveredFeedByUrlAndReference(EXISTING_FEED_URL, resource.page)).thenReturn(Future.successful(None))
    when(mongoRepository.getFeedByUrl(EXISTING_FEED_URL)).thenReturn(Future.successful(Some(mock(classOf[Feed]))))

    feedAutodiscoveryProcesser.process(resource, pageContent, DateTime.now)

    verify(mongoRepository, never).saveDiscoveredFeed(any(classOf[DiscoveredFeed]))
  }

}
