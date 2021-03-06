package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.model.{User, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlParser
import org.junit.Assert._
import org.junit.Test
import org.mockito.Mockito.{mock, when}

import scala.concurrent.Future

class PublisherGuessingServiceTest {

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val publisherGuessingService = new PublisherGuessingService(contentRetrievalService, new UrlParser)

  private val adminUser = User(admin = true)

  @Test
  def shouldNotMatchIfNoMatchingPublishers() {
    when(contentRetrievalService.getWebsitesByHostname("www.spammer.com", Some(adminUser))).thenReturn(Future.successful(Seq.empty))

    assertEquals(None, publisherGuessingService.guessPublisherBasedOnUrl("http://www.spammer.com", Some(adminUser)))
  }

  @Test
  def shouldMatchIfMultipleAvailable() {
    val golfCourseSite = Website(page = "http://www.wellington.govt.nz/services/berhgolf/index.html")
    val heritageInventory = Website(page = "http://www.wellington.govt.nz/services/heritage/inventory/index.html")
    val wccMainSite = Website(page = "http://www.wellington.govt.nz")

    val possiblePublishers = Seq(golfCourseSite, heritageInventory, wccMainSite)
    when(contentRetrievalService.getWebsitesByHostname("www.wellington.govt.nz", Some(adminUser))).thenReturn(Future.successful(possiblePublishers))

    assertEquals(Some(wccMainSite), publisherGuessingService.guessPublisherBasedOnUrl("http://www.wellington.govt.nz/news/display-item.php?id=3542", Some(adminUser)))
  }

  @Test
  def shouldMatchIfOnlyOnePossiblePublisher() {
    val wellingtonista = Website(title = Some("The Wellingtonista"), page = "http://www.wellingtonista.com")
    val possiblePublishers = Seq(wellingtonista)
    when(contentRetrievalService.getWebsitesByHostname("www.wellingtonista.com", Some(adminUser))).thenReturn(Future.successful(possiblePublishers))

    assertEquals(Some(wellingtonista), publisherGuessingService.guessPublisherBasedOnUrl("http://www.wellingtonista.com/a-week-of-it", Some(adminUser)))
  }

  @Test
  def shouldNotMatchJustBecauseTheHostNameMatches() {
    val hostedOne = Website(page = "http://homepages.paradise.net.nz/~titahi/")
    val hostedTwo = Website(page = "http://homepages.ihug.co.nz/~waicoll/")

    val possiblePublishers = Seq(hostedOne, hostedTwo)
    when(contentRetrievalService.getWebsitesByHostname("homepages.ihug.co.nz", Some(adminUser))).thenReturn(Future.successful(possiblePublishers))

    assertEquals(None, publisherGuessingService.guessPublisherBasedOnUrl("http://homepages.ihug.co.nz/~spammer/", Some(adminUser)))
  }

}