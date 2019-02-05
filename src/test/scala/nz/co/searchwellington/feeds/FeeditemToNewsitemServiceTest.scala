package nz.co.searchwellington.feeds

import org.junit.Assert.assertEquals
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.model.frontend.FrontendFeed
import nz.co.searchwellington.utils.TextTrimmer
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.when
import org.mockito.MockitoAnnotations
import uk.co.eelpieconsulting.common.geo.model.Place
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

class FeeditemToNewsitemServiceTest {
  @Mock private[feeds] val textTrimmer: TextTrimmer = null
  @Mock private[feeds] val place: Place = null
  @Mock private val feed: Feed = null
  private var service: FeeditemToNewsitemService = null

  @Before def setup {
    MockitoAnnotations.initMocks(this)
    service = new FeeditemToNewsitemService(textTrimmer, new PlaceToGeocodeMapper)
  }

  @Test
  @throws[Exception]
  def shouldSetGeocodeWhenAcceptingFeedNewsitem {
    when(place.getAddress).thenReturn("A place")
    val frontendFeed = new FrontendFeed
    val feedNewsitem = new FeedItem()
    feedNewsitem.setPlace(place)

    val newsitem = service.makeNewsitemFromFeedItem(feedNewsitem, Some(feed))

    assertEquals(Some("A place"), newsitem.geocode.map(_.getAddress))
  }

}
