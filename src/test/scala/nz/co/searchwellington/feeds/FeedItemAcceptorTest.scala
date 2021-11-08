package nz.co.searchwellington.feeds

import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, User}
import nz.co.searchwellington.urls.UrlCleaner
import org.joda.time.DateTime
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.Test
import org.mockito.Mockito.{mock, when}
import reactivemongo.api.bson.BSONObjectID

class FeedItemAcceptorTest {

  private val feed = Feed(publisher = Some(BSONObjectID.generate))
  private val feedReadingUser = User(name = Some("Feed reading user"))
  private val feeditemToNewsItemService = new FeeditemToNewsitemService(new PlaceToGeocodeMapper)
  private val urlCleaner = mock(classOf[UrlCleaner])

  private val feedItemAcceptor = new FeedItemAcceptor(feeditemToNewsItemService, urlCleaner)

  @Test
  def shouldSetAcceptedTimeWhenAccepting(): Unit = {
    val feedItem = FeedItem(id = "", title = Some("A headline"), url ="",  subscriptionId = "", body = None)
    val before = DateTime.now

    val acceptedNewsitem = feedItemAcceptor.acceptFeedItem(feedReadingUser, (feedItem, feed))

    assertTrue(acceptedNewsitem.accepted.nonEmpty)
    assertFalse(acceptedNewsitem.accepted.get.before(before.toDate))
  }

  def shouldCleanUrlWhenAccepting(): Unit = {
    val feedItem = FeedItem(id = "", title = Some("A headline"), url ="https://localhost/blah?PHPSESSION=123",  subscriptionId = "", body = None)
    when(urlCleaner.cleanSubmittedItemUrl("https://localhost/blah?PHPSESSION=123")).thenReturn("https://localhost/blah")

    val acceptedNewsitem = feedItemAcceptor.acceptFeedItem(feedReadingUser, (feedItem, feed))

    assertEquals("https://localhost/blah", acceptedNewsitem.page)
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

  @Test
  def shouldOverrideTheFeedItemDateIfFeedAcceptancePolicyIsToIgnoreFeedItemsDates(): Unit = {
    // Some feeds contains items with dates that refer to an event date in the future rather than news item publication date.
    // These items can clog up the head of the main feed so we should set their dates to the acceptance time rather than the state date.
    val feedItemWithFutureDate = FeedItem(id = "", title = Some("HEADLINE"), url ="",  subscriptionId = "", body = None, date = Some(DateTime.now.plusMonths(1).toDateTime()))
    val feedWithIgnoreDatesAcceptancePolicy = Feed(publisher = Some(BSONObjectID.generate), acceptance = FeedAcceptancePolicy.ACCEPT_IGNORING_DATE)

    val accepted = feedItemAcceptor.acceptFeedItem(feedReadingUser, (feedItemWithFutureDate, feedWithIgnoreDatesAcceptancePolicy))

    assertEquals(accepted.accepted, accepted.date)
  }

  @Test
  def acceptedFeedItemsWithNoDatesShouldDefaultToToday(): Unit = {
    val feedItemWithNoDate = FeedItem(id = "", title = Some("HEADLINE"), url ="",  subscriptionId = "", body = None, date = None)

    val accepted = feedItemAcceptor.acceptFeedItem(feedReadingUser, (feedItemWithNoDate, feed))

    assertFalse(accepted.date.isEmpty)
  }

}
