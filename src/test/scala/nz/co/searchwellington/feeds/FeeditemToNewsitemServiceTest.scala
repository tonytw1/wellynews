package nz.co.searchwellington.feeds

import nz.co.searchwellington.feeds.whakaoko.model.{FeedItem, LatLong, Place}
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.urls.UrlCleaner
import nz.co.searchwellington.urls.shorturls.CachingShortUrlResolverService
import org.junit.jupiter.api.Assertions.{assertEquals, assertNotNull, assertTrue}
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, when}
import reactivemongo.api.bson.BSONObjectID

import java.net.URL

class FeeditemToNewsitemServiceTest {

  private val cachingShortUrlResolverService = mock(classOf[CachingShortUrlResolverService])
  private val urlCleaner = new UrlCleaner(cachingShortUrlResolverService)

  private val feed = Feed(publisher = Some(BSONObjectID.generate))

  private val feeditemToNewsitemService = new FeeditemToNewsitemService(new PlaceToGeocodeMapper, urlCleaner)

  when(cachingShortUrlResolverService.resolveUrl(new URL("http://localhost/123"))).thenReturn(new URL("http://localhost/123"))

  @Test
  def shouldFlattenLoudHeadlinesWhenConvertingToNewsitem(): Unit = {
    val newsitemWithLoudCapsHeadline = FeedItem(id = "", title = Some("HEADLINE"), body = None, subscriptionId = "123", url = "http://localhost/123")

    val newsitem = feeditemToNewsitemService.makeNewsitemFromFeedItem(newsitemWithLoudCapsHeadline, feed).get

    assertEquals("Headline", newsitem.title)
  }

  @Test
  def shouldResolveFeedItemShoreUrlsWhenMappingToNewsitem(): Unit = {
    val newsitemWithLoudCapsHeadline = FeedItem(id = "", title = Some("Headline"), body = None, subscriptionId = "123", url = "https://localhost/blah?PHPSESSION=123")
    when(cachingShortUrlResolverService.resolveUrl(new URL("https://localhost/blah?PHPSESSION=123"))).thenReturn( new URL("https://localhost/blah"))

    val newsitem = feeditemToNewsitemService.makeNewsitemFromFeedItem(newsitemWithLoudCapsHeadline, feed).get

    assertEquals("https://localhost/blah", newsitem.page)
  }

  @Test
  def shouldSetGeocodeWhenAcceptingFeedNewsitem(): Unit = {
    val feedItemLatLong = LatLong(51.3, -0.3)
    val place = Place(latLong = Some(feedItemLatLong))
    val feedNewsitem = FeedItem(id = "", url = "http://localhost/123", subscriptionId = "", place = Some(place))

    val newsitem = feeditemToNewsitemService.makeNewsitemFromFeedItem(feedNewsitem, feed).get

    assertTrue(newsitem.geocode.nonEmpty)
    assertEquals(None, newsitem.geocode.get.address)
    assertEquals(feedItemLatLong.latitude, newsitem.geocode.get.latLong.get.latitude, 0)
    assertEquals(feedItemLatLong.longitude, newsitem.geocode.get.latLong.get.longitude, 0)
  }

  @Test
  def shouldPropagateFeedPublisherWhenAcceptingNewsitem(): Unit = {
    val feedNewsitem = FeedItem(id = "", url = "http://localhost/123", subscriptionId = "")

    val newsitem = feeditemToNewsitemService.makeNewsitemFromFeedItem(feedNewsitem, feed).get

    assertTrue(feed.publisher.nonEmpty)
    assertEquals(feed.publisher, newsitem.publisher)
  }

  @Test
  def shouldRecordSourceFeedWithAcceptingNewsitem(): Unit = {
    val feedNewsitem = FeedItem(id = "", url = "http://localhost/123", subscriptionId = "")

    val newsitem = feeditemToNewsitemService.makeNewsitemFromFeedItem(feedNewsitem, feed).get

    assertNotNull(feed._id)
    assertTrue(newsitem.feed.nonEmpty)
    assertEquals(Some(feed._id), newsitem.feed)
  }

  @Test
  def shouldNotSetAcceptanceDetails(): Unit = {
    val feedNewsitem = FeedItem(id = "", url = "http://localhost/123", subscriptionId = "")

    val newsitem = feeditemToNewsitemService.makeNewsitemFromFeedItem(feedNewsitem, feed).get

    assertTrue(newsitem.accepted.isEmpty)
    assertTrue(newsitem.acceptedBy.isEmpty)
  }

}
