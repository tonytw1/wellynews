package nz.co.searchwellington.feeds

import nz.co.searchwellington.feeds.whakaoko.model.{FeedItem, LatLong, Place}
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.urls.UrlCleaner
import org.junit.Assert.{assertEquals, assertNotNull, assertTrue}
import org.junit.Test
import org.mockito.Mockito.{mock, when}
import reactivemongo.api.bson.BSONObjectID

class FeeditemToNewsitemServiceTest {

  private val urlCleaner = mock(classOf[UrlCleaner])

  private val feed = Feed(publisher = Some(BSONObjectID.generate))

  private val service = new FeeditemToNewsitemService(new PlaceToGeocodeMapper, urlCleaner)

  @Test
  def shouldFlattenLoudHeadlinesWhenConvertingToNewsitem(): Unit = {
    val newsitemWithLoudCapsHeadline = FeedItem(id = "", title = Some("HEADLINE"), body = None, subscriptionId = "123", url = "")

    val newsitem = service.makeNewsitemFromFeedItem(newsitemWithLoudCapsHeadline, feed)

    assertEquals(Some("Headline"), newsitem.title)
  }

  @Test
  def shouldCleanUrlToNewsitem(): Unit = {
    val newsitemWithLoudCapsHeadline = FeedItem(id = "", title = Some("Headline"), body = None, subscriptionId = "123", url = "https://localhost/blah?PHPSESSION=123")
    when(urlCleaner.cleanSubmittedItemUrl( "https://localhost/blah?PHPSESSION=123")).thenReturn( "https://localhost/blah")

    val newsitem = service.makeNewsitemFromFeedItem(newsitemWithLoudCapsHeadline, feed)

    assertEquals("https://localhost/blah", newsitem.page)
  }

  @Test
  def shouldSetGeocodeWhenAcceptingFeedNewsitem(): Unit = {
    val feedItemLatLong = LatLong(51.3, -0.3)
    val place = Place(latLong = Some(feedItemLatLong))
    val feedNewsitem = FeedItem(id = "", url = "", subscriptionId = "", place = Some(place))

    val newsitem = service.makeNewsitemFromFeedItem(feedNewsitem, feed)

    assertTrue(newsitem.geocode.nonEmpty)
    assertEquals(None, newsitem.geocode.get.address)
    assertEquals(feedItemLatLong.latitude, newsitem.geocode.get.latitude.get, 0)
    assertEquals(feedItemLatLong.longitude, newsitem.geocode.get.longitude.get, 0)
  }

  @Test
  def shouldPropogateFeedPublisherWhenAcceptingNewsitem(): Unit = {
    val feedNewsitem = FeedItem(id = "", url = "", subscriptionId = "")

    val newsitem = service.makeNewsitemFromFeedItem(feedNewsitem, feed)

    assertTrue(feed.publisher.nonEmpty)
    assertEquals(feed.publisher, newsitem.publisher)
  }

  @Test
  def shouldRecordSourceFeedWithAcceptingNewsitem(): Unit = {
    val feedNewsitem = FeedItem(id = "", url = "", subscriptionId = "")

    val newsitem = service.makeNewsitemFromFeedItem(feedNewsitem, feed)

    assertNotNull(feed._id)
    assertTrue(newsitem.feed.nonEmpty)
    assertEquals(Some(feed._id), newsitem.feed)
  }

  @Test
  def shouldNotSetAcceptanceDetails(): Unit = {
    val feedNewsitem = FeedItem(id = "", url = "", subscriptionId = "")

    val newsitem = service.makeNewsitemFromFeedItem(feedNewsitem, feed)

    assertTrue(newsitem.accepted.isEmpty)
    assertTrue(newsitem.acceptedBy.isEmpty)
  }

}
