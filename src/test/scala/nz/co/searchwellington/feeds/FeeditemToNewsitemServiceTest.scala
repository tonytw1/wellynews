package nz.co.searchwellington.feeds

import nz.co.searchwellington.feeds.whakaoko.model.{FeedItem, LatLong, Place}
import nz.co.searchwellington.model.Feed
import org.junit.Assert.{assertEquals, assertNotNull, assertTrue}
import org.junit.Test
import reactivemongo.api.bson.BSONObjectID

class FeeditemToNewsitemServiceTest {
  private val feed: Feed = Feed(publisher = Some(BSONObjectID.generate))

  private val service = new FeeditemToNewsitemService(new PlaceToGeocodeMapper)

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
