package nz.co.searchwellington.linkchecking

import java.util
import java.util.UUID

import nz.co.searchwellington.commentfeeds.{CommentFeedDetectorService, CommentFeedGuesserService}
import nz.co.searchwellington.htmlparsing.CompositeLinkExtractor
import nz.co.searchwellington.model.{DiscoveredFeed, Feed, Newsitem}
import nz.co.searchwellington.repositories.ResourceFactory
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Assert.assertTrue
import org.junit.Test
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
  private val resourceFactory = mock(classOf[ResourceFactory])

  private val resource = Newsitem(id = UUID.randomUUID().toString, page = Some("http://localhost/test"))
  private val pageContent = "Meh"
  private var feedAutodiscoveryProcesser = new FeedAutodiscoveryProcesser(mongoRepository, linkExtractor, commentFeedDetector, commentFeedGuesser, resourceFactory)

  @Test def newlyDiscoveredFeedsUrlsShouldBeRecordedAsDiscoveredFeeds(): Unit = {
    val autoDiscoveredLinks = new util.HashSet[String]  // TODO Scala collection
    autoDiscoveredLinks.add(UNSEEN_FEED_URL)
    when(linkExtractor.extractLinks(pageContent)).thenReturn(autoDiscoveredLinks)

    when(commentFeedDetector.isCommentFeedUrl(UNSEEN_FEED_URL)).thenReturn(false)
    when(mongoRepository.getDiscoveredFeedByUrl(UNSEEN_FEED_URL)).thenReturn(Future.successful(None))
    when(mongoRepository.getFeedByUrl(UNSEEN_FEED_URL)).thenReturn(Future.successful(None))

    val newlyDiscoveredFeed = DiscoveredFeed(url = UNSEEN_FEED_URL)
    when(resourceFactory.createNewDiscoveredFeed(UNSEEN_FEED_URL)).thenReturn(newlyDiscoveredFeed)

    feedAutodiscoveryProcesser.process(resource, pageContent)

    assertTrue(newlyDiscoveredFeed.references.contains(resource.page.get))
    verify(mongoRepository).saveDiscoveredFeed(newlyDiscoveredFeed)
  }

  @Test
  def doNotRecordDiscoveredFeedsIfWeAlreadyHaveThisFeed(): Unit = {
    val autoDiscoveredLinks = new util.HashSet[String]
    autoDiscoveredLinks.add(EXISTING_FEED_URL)
    when(linkExtractor.extractLinks(pageContent)).thenReturn(autoDiscoveredLinks)

    when(commentFeedDetector.isCommentFeedUrl(EXISTING_FEED_URL)).thenReturn(false)

    val newlyDiscoveredFeed = new DiscoveredFeed(url = EXISTING_FEED_URL)
    when(mongoRepository.getDiscoveredFeedByUrl(EXISTING_FEED_URL)).thenReturn(Future.successful(None))
    when(mongoRepository.getFeedByUrl(EXISTING_FEED_URL)).thenReturn(Future.successful(Some(mock(classOf[Feed]))))

    feedAutodiscoveryProcesser.process(resource, pageContent)

    verify(resourceFactory, never).createNewDiscoveredFeed(any(classOf[String]))
    verify(mongoRepository, never).saveDiscoveredFeed(any(classOf[DiscoveredFeed]))
  }

}
