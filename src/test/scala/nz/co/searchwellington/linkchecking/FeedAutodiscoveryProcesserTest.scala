package nz.co.searchwellington.linkchecking

import org.junit.Assert.assertTrue
import org.mockito.Matchers.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import java.util

import nz.co.searchwellington.commentfeeds.CommentFeedDetectorService
import nz.co.searchwellington.commentfeeds.CommentFeedGuesserService
import nz.co.searchwellington.htmlparsing.CompositeLinkExtractor
import nz.co.searchwellington.model.{DiscoveredFeed, Feed, Newsitem, Resource}
import nz.co.searchwellington.repositories.HibernateResourceDAO
import nz.co.searchwellington.repositories.ResourceFactory
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class FeedAutodiscoveryProcesserTest {

  private val UNSEEN_FEED_URL = "http://something/new"
  private val EXISTING_FEED_URL = "http://something/old"

  @Mock val resourceDAO: HibernateResourceDAO = null
  @Mock val linkExtractor: CompositeLinkExtractor = null
  @Mock val commentFeedDetector: CommentFeedDetectorService = null
  @Mock val commentFeedGuesser: CommentFeedGuesserService = null
  @Mock private val resourceFactory: ResourceFactory = null
  val resource = Newsitem()
  @Mock val existingFeed: Feed = null
  private val pageContent = "Meh"
  private var feedAutodiscoveryProcesser: FeedAutodiscoveryProcesser = null

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)
    feedAutodiscoveryProcesser = new FeedAutodiscoveryProcesser(resourceDAO, linkExtractor, commentFeedDetector, commentFeedGuesser, resourceFactory)
  }

  @Test def newlyDiscoveredFeedsUrlsShouldBeRecordedAsDiscoveredFeeds(): Unit = {
    val autoDiscoveredLinks = new util.HashSet[String]
    autoDiscoveredLinks.add(UNSEEN_FEED_URL)
    when(linkExtractor.extractLinks(pageContent)).thenReturn(autoDiscoveredLinks)
    when(commentFeedDetector.isCommentFeedUrl(UNSEEN_FEED_URL)).thenReturn(false)
    when(resourceDAO.loadDiscoveredFeedByUrl(UNSEEN_FEED_URL)).thenReturn(null)
    val newlyDiscoveredFeed = new DiscoveredFeed
    newlyDiscoveredFeed.setReferences(new util.HashSet[Resource])
    when(resourceFactory.createNewDiscoveredFeed(UNSEEN_FEED_URL)).thenReturn(newlyDiscoveredFeed)

    feedAutodiscoveryProcesser.process(resource, pageContent)

    assertTrue(newlyDiscoveredFeed.getReferences.contains(resource))
    verify(resourceDAO).saveDiscoveredFeed(newlyDiscoveredFeed)
  }

  @Test def doNotRecordDiscoveredFeedsIfWeAlreadyHaveThisFeed(): Unit = {
    val autoDiscoveredLinks = new util.HashSet[String]
    autoDiscoveredLinks.add(EXISTING_FEED_URL)
    when(linkExtractor.extractLinks(pageContent)).thenReturn(autoDiscoveredLinks)
    when(commentFeedDetector.isCommentFeedUrl(EXISTING_FEED_URL)).thenReturn(false)
    val newlyDiscoveredFeed = new DiscoveredFeed
    newlyDiscoveredFeed.setUrl(EXISTING_FEED_URL)
    newlyDiscoveredFeed.setReferences(new util.HashSet[Resource])
    when(resourceDAO.loadDiscoveredFeedByUrl(EXISTING_FEED_URL)).thenReturn(newlyDiscoveredFeed)
    when(resourceDAO.loadFeedByUrl(EXISTING_FEED_URL)).thenReturn(existingFeed)

    feedAutodiscoveryProcesser.process(resource, pageContent)

    verify(resourceFactory, never).createNewDiscoveredFeed(any(classOf[String]))
    verify(resourceDAO, never).saveDiscoveredFeed(any(classOf[DiscoveredFeed]))
  }

}
