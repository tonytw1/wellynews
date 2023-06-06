package nz.co.searchwellington.repositories.elasticsearch

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{User, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlParser
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, when}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class PublisherGuessingServiceTest extends ReasonableWaits {

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val publisherGuessingService = new PublisherGuessingService(contentRetrievalService, new UrlParser)

  private val adminUser = User(admin = true)

  private implicit val currentSpan: Span = Span.current()

  @Test
  def shouldNotMatchIfNoMatchingPublishers(): Unit = {
    when(contentRetrievalService.getWebsitesByHostname("www.spammer.com", Some(adminUser))).thenReturn(Future.successful(Seq.empty))

    val guess = Await.result(publisherGuessingService.guessPublisherBasedOnUrl("http://www.spammer.com", Some(adminUser)), TenSeconds)

    assertTrue(guess.isEmpty)
  }

  @Test
  def shouldMatchIfMultipleAvailable(): Unit = {
    val golfCourseSite = Website(page = "http://www.wellington.govt.nz/services/berhgolf/index.html")
    val heritageInventory = Website(page = "http://www.wellington.govt.nz/services/heritage/inventory/index.html")
    val wccMainSite = Website(page = "http://www.wellington.govt.nz")

    val possiblePublishers = Seq(golfCourseSite, heritageInventory, wccMainSite)
    when(contentRetrievalService.getWebsitesByHostname("www.wellington.govt.nz", Some(adminUser))).thenReturn(Future.successful(possiblePublishers))

    val guess = Await.result(publisherGuessingService.guessPublisherBasedOnUrl("http://www.wellington.govt.nz/news/display-item.php?id=3542", Some(adminUser)), TenSeconds)

    assertEquals(Some(wccMainSite), guess)
  }

  @Test
  def shouldMatchIfOnlyOnePossiblePublisher(): Unit = {
    val wellingtonista = Website(title = "The Wellingtonista", page = "http://www.wellingtonista.com")
    val possiblePublishers = Seq(wellingtonista)
    when(contentRetrievalService.getWebsitesByHostname("www.wellingtonista.com", Some(adminUser))).thenReturn(Future.successful(possiblePublishers))

    val guess = Await.result(publisherGuessingService.guessPublisherBasedOnUrl("http://www.wellingtonista.com/a-week-of-it", Some(adminUser)), TenSeconds)

    assertEquals(Some(wellingtonista), guess)
  }

  @Test
  def shouldNotMatchJustBecauseTheHostNameMatches(): Unit = {
    val hostedOne = Website(page = "http://homepages.paradise.net.nz/~titahi/")
    val hostedTwo = Website(page = "http://homepages.ihug.co.nz/~waicoll/")

    val possiblePublishers = Seq(hostedOne, hostedTwo)
    when(contentRetrievalService.getWebsitesByHostname("homepages.ihug.co.nz", Some(adminUser))).thenReturn(Future.successful(possiblePublishers))

    val guess = Await.result(publisherGuessingService.guessPublisherBasedOnUrl("http://homepages.ihug.co.nz/~spammer/", Some(adminUser)), TenSeconds)

    assertTrue(guess.isEmpty)
  }

}