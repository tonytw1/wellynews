package nz.co.searchwellington.feeds

import java.util.UUID

import nz.co.searchwellington.model.{Feed, Newsitem, User}
import org.junit.Before
import org.mockito.{Mock, MockitoAnnotations}

class FeedItemAcceptorTest {
  @Mock private[feeds] val feednewsitem = null
  private val feed = Feed(id = UUID.randomUUID().toString, title = Some("A feed"))
  private val feedNewsitem = Newsitem(id = UUID.randomUUID().toString, title = Some("HEADLINE"))
  @Mock private[feeds] val publisher = null
  private var feedItemAcceptor: FeedItemAcceptor = null
  private val user = User(id = "123")

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)
    feedItemAcceptor = new FeedItemAcceptor(null)
  }

  /*
  @Test
  @throws[Exception]
  def shouldSetAcceptedTimeWhenAccepting(): Unit = {
    val accepted = feedItemAcceptor.acceptFeedItem(user, feednewsitem, null)

    assertTrue(accepted.accepted2.nonEmpty)
  }

  @Test
  @throws[Exception]
  def shouldSetAcceptedByUserAndOwnerWhenAccepting(): Unit = {
    val accepted = feedItemAcceptor.acceptFeedItem(user, feednewsitem, null)

    assertEquals(Some(user.id), accepted.acceptedBy)
    assertEquals(Some(user.id), accepted.owner)
  }

  @Test
  @throws[Exception]
  def shouldFlattenLoudHeadlinesWhenAccepting(): Unit = {
    val accepted = feedItemAcceptor.acceptFeedItem(user, feednewsitem, null)
    assertEquals("Headline", accepted.title.get)
  }
  */

}
