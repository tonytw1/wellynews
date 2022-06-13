package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, Newsitem, User}
import nz.co.searchwellington.urls.UrlCleaner
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse, assertTrue}
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, when}
import reactivemongo.api.bson.BSONObjectID

class FeedItemAcceptorTest {

  private val feed = Feed(publisher = Some(BSONObjectID.generate))
  private val feedReadingUser = User(name = Some("Feed reading user"))
  private val urlCleaner = mock(classOf[UrlCleaner])

  private val feedItemAcceptor = new FeedItemAcceptor

  @Test
  def shouldSetAcceptedTimeWhenAccepting(): Unit = {
    val newsitem = Newsitem(id = "", title = "A headline", description = None)
    val before = DateTime.now

    val acceptedNewsitem = feedItemAcceptor.acceptFeedItem(feedReadingUser, (newsitem, feed))

    assertTrue(acceptedNewsitem.accepted.nonEmpty)
    assertFalse(acceptedNewsitem.accepted.get.before(before.toDate))
  }

  def shouldCleanUrlWhenAccepting(): Unit = {
    val newsitem = Newsitem(id = "", title = "A headline", page ="https://localhost/blah?PHPSESSION=123", description = None)
    when(urlCleaner.cleanSubmittedItemUrl("https://localhost/blah?PHPSESSION=123")).thenReturn("https://localhost/blah")

    val acceptedNewsitem = feedItemAcceptor.acceptFeedItem(feedReadingUser, (newsitem, feed))

    assertEquals("https://localhost/blah", acceptedNewsitem.page)
  }

  @Test
  def shouldSetAcceptedByUserAndOwnerWhenAccepting(): Unit = {
    val newsitem = Newsitem(id = "", title = "A headline", description = None)

    val acceptedNewsitem = feedItemAcceptor.acceptFeedItem(feedReadingUser, (newsitem, feed))

    assertEquals(Some(feedReadingUser._id), acceptedNewsitem.acceptedBy)
    assertEquals(Some(feedReadingUser._id), acceptedNewsitem.owner)
  }

  @Test
  def shouldOverrideTheFeedItemDateIfFeedAcceptancePolicyIsToIgnoreFeedItemsDates(): Unit = {
    // Some feeds contains items with dates that refer to an event date in the future rather than news item publication date.
    // These items can clog up the head of the main feed so we should set their dates to the acceptance time rather than the state date.
    val newsitemWithFutureDate = Newsitem(id = "", title = "HEADLINE", description = None, date = Some(DateTime.now.plusMonths(1).toDate))
    val feedWithIgnoreDatesAcceptancePolicy = Feed(publisher = Some(BSONObjectID.generate), acceptance = FeedAcceptancePolicy.ACCEPT_IGNORING_DATE)

    val accepted = feedItemAcceptor.acceptFeedItem(feedReadingUser, (newsitemWithFutureDate, feedWithIgnoreDatesAcceptancePolicy))

    assertEquals(accepted.accepted, accepted.date)
  }

  @Test
  def acceptedFeedItemsWithNoDatesShouldDefaultToToday(): Unit = {
    val feedItemWithNoDate = Newsitem(id = "", title = "HEADLINE", description = None, date = None)

    val accepted = feedItemAcceptor.acceptFeedItem(feedReadingUser, (feedItemWithNoDate, feed))

    assertFalse(accepted.date.isEmpty)
  }

}
