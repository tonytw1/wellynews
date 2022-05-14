package nz.co.searchwellington.commentfeeds.detectors

import nz.co.searchwellington.model.Newsitem
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Assert.{assertFalse, assertTrue}
import org.junit.Test
import org.mockito.Mockito.{mock, when}

import java.net.URL
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ExistingNewsitemCommentFeedDetectorTest {

  private val mongoRepository = mock(classOf[MongoRepository])

  @Test
  def shouldDetectSlashFeedSuffiOfExistingNewsitemsAsCommentFeed(): Unit = {
    when(mongoRepository.getResourceByUrl("https://wtmc.org.nz/trip-report/st-arnaud-range-and-the-camel")).thenReturn(Future.successful(None))
    when(mongoRepository.getResourceByUrl("https://wtmc.org.nz/trip-report/st-arnaud-range-and-the-camel/")).thenReturn(Future.successful(Some(Newsitem())))

    val existing = new ExistingNewsitemCommentFeedDetector(mongoRepository)

    assertTrue(existing.isValid(new URL("https://wtmc.org.nz/trip-report/st-arnaud-range-and-the-camel/feed/")))
  }

  @Test
  def shouldNotObjectToSlashFeedsWithNoExistingNewsitem(): Unit = {
    when(mongoRepository.getResourceByUrl("https://wtmc.org.nz/trip-report/st-arnaud-range-and-the-camel")).thenReturn(Future.successful(None))
    when(mongoRepository.getResourceByUrl("https://wtmc.org.nz/trip-report/st-arnaud-range-and-the-camel/")).thenReturn(Future.successful(None))

    val existing = new ExistingNewsitemCommentFeedDetector(mongoRepository)

    assertFalse(existing.isValid(new URL("https://wtmc.org.nz/trip-report/st-arnaud-range-and-the-camel/feed/")))
  }

}
