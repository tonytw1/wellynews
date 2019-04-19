package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.{Feed, Newsitem, User}
import nz.co.searchwellington.utils.TextTrimmer
import org.joda.time.DateTime
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.Test
import org.mockito.Mockito.{mock, when}
import reactivemongo.bson.BSONObjectID
import uk.co.eelpieconsulting.common.geo.model.Place
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

class FeedItemAcceptorTest {
  private val textTrimmer = mock(classOf[TextTrimmer])
  private val place = mock(classOf[Place])

  private val feed = Feed(publisher = Some(BSONObjectID.generate))
  private val feedReadingUser = User(name = Some("Feed reading feedReadingUser"))
  private val feeditemToNewsItemSerice = mock(classOf[FeeditemToNewsitemService])

  private val service = new FeedItemAcceptor(feeditemToNewsItemSerice)

  @Test
  def shouldSetAcceptedTimeWhenAccepting(): Unit = {
    val feedItem = mock(classOf[FeedItem])
    when(feeditemToNewsItemSerice.makeNewsitemFromFeedItem(feedItem, feed)).thenReturn(Newsitem())
    val before = DateTime.now

    val acceptedNewsitem = service.acceptFeedItem(feedReadingUser, (feedItem, feed))

    assertTrue(acceptedNewsitem.accepted.nonEmpty)
    assertFalse(acceptedNewsitem.accepted.get.before(before.toDate))
  }

  @Test
  def shouldSetAcceptedByUserAndOwnerWhenAccepting(): Unit = {
    val feedItem = mock(classOf[FeedItem])
    when(feeditemToNewsItemSerice.makeNewsitemFromFeedItem(feedItem, feed)).thenReturn(Newsitem())

    val acceptedNewsitem = service.acceptFeedItem(feedReadingUser, (feedItem, feed))

    assertEquals(Some(feedReadingUser._id), acceptedNewsitem.acceptedBy)
    assertEquals(Some(feedReadingUser._id), acceptedNewsitem.owner)
  }

  /*
  @Test
  @throws[Exception]
  def shouldFlattenLoudHeadlinesWhenAccepting(): Unit = {
    val accepted = feedItemAcceptor.acceptFeedItem(feedReadingUser, feednewsitem, null)
    assertEquals("Headline", accepted.title.get)
  }
  */

}
