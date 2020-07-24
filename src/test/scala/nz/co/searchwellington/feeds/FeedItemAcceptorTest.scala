package nz.co.searchwellington.feeds

import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, User}
import org.joda.time.DateTime
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.Test
import reactivemongo.bson.BSONObjectID

class FeedItemAcceptorTest {

  private val feed = Feed(publisher = Some(BSONObjectID.generate))
  private val feedReadingUser = User(name = Some("Feed reading user"))
  private val feeditemToNewsItemService = new FeeditemToNewsitemService(new PlaceToGeocodeMapper)

  private val feedItemAcceptor = new FeedItemAcceptor(feeditemToNewsItemService)

  @Test
  def shouldSetAcceptedTimeWhenAccepting(): Unit = {
    val feedItem = FeedItem(id = "", title = Some("A headline"), url ="",  subscriptionId = "", body = None)
    val before = DateTime.now

    val acceptedNewsitem = feedItemAcceptor.acceptFeedItem(feedReadingUser, (feedItem, feed))

    assertTrue(acceptedNewsitem.accepted.nonEmpty)
    assertFalse(acceptedNewsitem.accepted.get.before(before.toDate))
  }

  @Test
  def shouldSetAcceptedByUserAndOwnerWhenAccepting(): Unit = {
    val feedItem = FeedItem(id = "", title = Some("A headline"), url ="",  subscriptionId = "", body = None)

    val acceptedNewsitem = feedItemAcceptor.acceptFeedItem(feedReadingUser, (feedItem, feed))

    assertEquals(Some(feedReadingUser._id), acceptedNewsitem.acceptedBy)
    assertEquals(Some(feedReadingUser._id), acceptedNewsitem.owner)
  }

  @Test
  def shouldFlattenLoudHeadlinesWhenAccepting(): Unit = {
    val feedItemWithLoudCapsHeadline = FeedItem(id = "", title = Some("HEADLINE"), url ="",  subscriptionId = "", body = None)

    val accepted = feedItemAcceptor.acceptFeedItem(feedReadingUser, (feedItemWithLoudCapsHeadline, feed))

    assertEquals("Headline", accepted.title.get)
  }

}
