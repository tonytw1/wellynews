package nz.co.searchwellington.linkchecking

import java.util
import java.util.UUID

import nz.co.searchwellington.commentfeeds.{CommentFeedDetectorService, CommentFeedGuesserService}
import nz.co.searchwellington.htmlparsing.CompositeLinkExtractor
import nz.co.searchwellington.model.{DiscoveredFeed, Feed, Newsitem}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{mock, never, verify, when}

import scala.concurrent.Future

class FeedAutodiscoveryProcesserTest {

  private val UNSEEN_FEED_URL = "http://something/new"
  private val EXISTING_FEED_URL = "http://something/old"

  private val mongoRepository = mock(classOf[MongoRepository])
  private val linkExtractor = mock(classOf[CompositeLinkExtractor])
  private val commentFeedDetector = mock(classOf[CommentFeedDetectorService])
  private val commentFeedGuesser = mock(classOf[CommentFeedGuesserService])

  private val resource = Newsitem(id = UUID.randomUUID().toString, page = Some("http://localhost/test"))
  private val pageContent = "Meh"
  private var feedAutodiscoveryProcesser = new FeedAutodiscoveryProcesser(mongoRepository, linkExtractor, commentFeedDetector, commentFeedGuesser)

  @Test def newlyDiscoveredFeedsUrlsShouldBeRecordedAsDiscoveredFeeds(): Unit = {
    val now = DateTime.now

    val autoDiscoveredLinks = Seq(UNSEEN_FEED_URL)
    when(linkExtractor.extractLinks(pageContent)).thenReturn(autoDiscoveredLinks)

    when(commentFeedDetector.isCommentFeedUrl(UNSEEN_FEED_URL)).thenReturn(false)
    when(mongoRepository.getDiscoveredFeedByUrlAndReference(UNSEEN_FEED_URL, resource.page.get)).thenReturn(Future.successful(None))
    when(mongoRepository.getFeedByUrl(UNSEEN_FEED_URL)).thenReturn(Future.successful(None))

    val saved = ArgumentCaptor.forClass(classOf[DiscoveredFeed])

    feedAutodiscoveryProcesser.process(resource, pageContent, now)

    verify(mongoRepository).saveDiscoveredFeed(saved.capture())
    assertEquals(UNSEEN_FEED_URL, saved.getValue.url)
    assertEquals(resource.page.get, saved.getValue.referencedFrom)
  }

  @Test
  def doNotRecordDiscoveredFeedsIfWeAlreadyHaveThisFeed(): Unit = {
    val now = DateTime.now

    val autoDiscoveredLinks = Seq(EXISTING_FEED_URL)
    when(linkExtractor.extractLinks(pageContent)).thenReturn(autoDiscoveredLinks)
    when(commentFeedDetector.isCommentFeedUrl(EXISTING_FEED_URL)).thenReturn(false)
    when(mongoRepository.getDiscoveredFeedByUrlAndReference(EXISTING_FEED_URL, resource.page.get)).thenReturn(Future.successful(None))
    when(mongoRepository.getFeedByUrl(EXISTING_FEED_URL)).thenReturn(Future.successful(Some(mock(classOf[Feed]))))

    feedAutodiscoveryProcesser.process(resource, pageContent, now)

    verify(mongoRepository, never).saveDiscoveredFeed(any(classOf[DiscoveredFeed]))
  }

}
