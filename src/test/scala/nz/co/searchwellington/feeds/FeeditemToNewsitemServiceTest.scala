package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.{Feed, User}
import nz.co.searchwellington.utils.TextTrimmer
import org.junit.Assert.{assertEquals, assertNotNull, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.when
import org.mockito.{Mock, MockitoAnnotations}
import reactivemongo.bson.BSONObjectID
import uk.co.eelpieconsulting.common.geo.model.Place
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

class FeeditemToNewsitemServiceTest {
  @Mock private[feeds] val textTrimmer: TextTrimmer = null
  @Mock private[feeds] val place: Place = null
  private val feed: Feed = Feed(publisher = Some(BSONObjectID.generate))
  private var service: FeeditemToNewsitemService = null
  private val user = User(name = Some("Feed reading user"))

  @Before def setup {
    MockitoAnnotations.initMocks(this)
    service = new FeeditemToNewsitemService(textTrimmer, new PlaceToGeocodeMapper)
  }

  @Test
  def shouldSetGeocodeWhenAcceptingFeedNewsitem {
    when(place.getAddress).thenReturn("A place")
    val feedNewsitem = new FeedItem()
    feedNewsitem.setPlace(place)

    val newsitem = service.makeNewsitemFromFeedItem(feedNewsitem, feed)

    assertEquals(Some("A place"), newsitem.geocode.map(_.getAddress))
  }

  @Test
  def shouldPropogateFeedPublisherWhenAcceptingNewsitem: Unit = {
    val feedNewsitem = new FeedItem()

    val newsitem = service.makeNewsitemFromFeedItem(feedNewsitem, feed)

    assertTrue(feed.publisher.nonEmpty)
    assertEquals(feed.publisher, newsitem.publisher)
  }

  @Test
  def shouldRecordSourceFeedWithAcceptingNewsitem: Unit = {
    val feedNewsitem = new FeedItem()

    val newsitem = service.makeNewsitemFromFeedItem(feedNewsitem, feed)

    assertNotNull(feed._id)
    assertTrue(newsitem.feed.nonEmpty)
    assertEquals(Some(feed._id), newsitem.feed)
  }

  @Test
  def shouldNotSetAcceptanceDetails(): Unit = {
    val feedNewsitem = new FeedItem()

    val newsitem = service.makeNewsitemFromFeedItem(feedNewsitem, feed)

    assertTrue(newsitem.accepted.isEmpty)
    assertTrue(newsitem.acceptedBy.isEmpty)
  }

}
