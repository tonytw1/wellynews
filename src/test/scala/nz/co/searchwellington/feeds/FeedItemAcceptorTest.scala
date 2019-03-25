package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.{Feed, Newsitem, User}
import nz.co.searchwellington.utils.TextTrimmer
import org.joda.time.DateTime
import org.junit.Assert.assertTrue
import org.junit.{Before, Test}
import org.mockito.Mockito.mock
import org.mockito.{Mock, MockitoAnnotations}
import reactivemongo.bson.BSONObjectID
import uk.co.eelpieconsulting.common.geo.model.Place
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem
import org.mockito.Mockito.when

class FeedItemAcceptorTest {
  @Mock private[feeds] val textTrimmer: TextTrimmer = null
  @Mock private[feeds] val place: Place = null
  private val feed: Feed = Feed(publisher = Some(BSONObjectID.generate))
  private var service: FeedItemAcceptor = null
  private val user = User(name = Some("Feed reading user"))

  private val feeditemToNewsItemSerice = mock(classOf[FeeditemToNewsitemService])

  @Before def setup {
    MockitoAnnotations.initMocks(this)
    service = new FeedItemAcceptor(feeditemToNewsItemSerice)
  }

  @Test
  def shouldSetNewsitemAcceptedTimeWhenAccepting(): Unit = {
    val feedNewsitem = new FeedItem()
    val newsitem = Newsitem()
    when(feeditemToNewsItemSerice.makeNewsitemFromFeedItem(feedNewsitem, feed)).thenReturn(newsitem)
    val before = DateTime.now

    val acceptedNewsitem = service.acceptFeedItem(user, (feedNewsitem, feed))

    assertTrue(acceptedNewsitem.accepted.nonEmpty)
    assertTrue(acceptedNewsitem.accepted.get.after(before.toDate))
  }

}
