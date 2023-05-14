package nz.co.searchwellington.feeds

import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, User}
import nz.co.searchwellington.urls.UrlCleaner
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse, assertTrue}
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, when}
import reactivemongo.api.bson.BSONObjectID

import java.net.URL

class FeedItemAcceptorTest {

  private val feed = Feed(publisher = Some(BSONObjectID.generate))
  private val feedReadingUser = User(name = Some("Feed reading user"))
  private val urlCleaner = mock(classOf[UrlCleaner])
  private val placeToGeocodeMapper = mock(classOf[PlaceToGeocodeMapper])

  private val feedItemAcceptor = new FeedItemAcceptor(new FeeditemToNewsitemService(placeToGeocodeMapper, urlCleaner))

  @Test
  def shouldSetAcceptedTimeWhenAccepting(): Unit = {
    val feedItem = FeedItem(id = "", title = Some("A headline"), url = "http://localhost/blah", subscriptionId = "a-subscription")
    val before = DateTime.now
    when(urlCleaner.cleanSubmittedItemUrl(new URL("http://localhost/blah"))).thenReturn(new URL("http://localhost/blah"))

    val acceptedNewsitem = feedItemAcceptor.acceptFeedItem(feedReadingUser, (feedItem, feed)).get

    assertTrue(acceptedNewsitem.accepted.nonEmpty)
    assertFalse(acceptedNewsitem.accepted.get.before(before.toDate))
  }

  def shouldCleanUrlWhenAccepting(): Unit = {
    val feedItem = FeedItem(id = "", title = Some("A headline"), url = "https://localhost/blah?PHPSESSION=123", subscriptionId = "a-subscription")
    when(urlCleaner.cleanSubmittedItemUrl(new URL("https://localhost/blah?PHPSESSION=123"))).thenReturn(new URL("https://localhost/blah"))

    val acceptedNewsitem = feedItemAcceptor.acceptFeedItem(feedReadingUser, (feedItem, feed)).get

    assertEquals("https://localhost/blah", acceptedNewsitem.page)
  }

  @Test
  def shouldSetAcceptedByUserAndOwnerWhenAccepting(): Unit = {
    val feedItem = FeedItem(id = "", title = Some("A headline"), url = "http://localhost/blah", subscriptionId = "a-subscription")
    when(urlCleaner.cleanSubmittedItemUrl(new URL("http://localhost/blah"))).thenReturn(new URL("http://localhost/blah"))

    val acceptedNewsitem = feedItemAcceptor.acceptFeedItem(feedReadingUser, (feedItem, feed)).get

    assertEquals(Some(feedReadingUser._id), acceptedNewsitem.acceptedBy)
    assertEquals(Some(feedReadingUser._id), acceptedNewsitem.owner)
  }

  @Test
  def shouldOverrideTheFeedItemDateIfFeedAcceptancePolicyIsToIgnoreFeedItemsDates(): Unit = {
    // Some feeds contains items with dates that refer to an event date in the future rather than news item publication date.
    // These items can clog up the head of the main feed so we should set their dates to the acceptance time rather than the state date.
    val feedItemWithFutureDate = FeedItem(id = "", title = Some("A headline"), url = "http://localhost/blah", subscriptionId = "a-subscription", date = Some(DateTime.now.plusMonths(1)))
    val feedWithIgnoreDatesAcceptancePolicy = Feed(publisher = Some(BSONObjectID.generate), acceptance = FeedAcceptancePolicy.ACCEPT_IGNORING_DATE)
    when(urlCleaner.cleanSubmittedItemUrl(new URL("http://localhost/blah"))).thenReturn(new URL("http://localhost/blah"))

    val accepted = feedItemAcceptor.acceptFeedItem(feedReadingUser, (feedItemWithFutureDate, feedWithIgnoreDatesAcceptancePolicy)).get

    assertEquals(accepted.accepted, accepted.date)
  }

  @Test
  def acceptedFeedItemsWithNoDatesShouldDefaultToToday(): Unit = {
    val feedItemWithNoDate = FeedItem(id = "", title = Some("A headline"), url = "http://localhost/blah", subscriptionId = "a-subscription", date = None)
    when(urlCleaner.cleanSubmittedItemUrl(new URL("http://localhost/blah"))).thenReturn(new URL("http://localhost/blah"))

    val accepted = feedItemAcceptor.acceptFeedItem(feedReadingUser, (feedItemWithNoDate, feed)).get

    assertFalse(accepted.date.isEmpty)
  }

}
