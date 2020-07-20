package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.model.Website
import nz.co.searchwellington.repositories.HibernateResourceDAO
import nz.co.searchwellington.urls.UrlParser
import org.junit.Assert._
import org.junit.Test
import org.mockito.Mockito.{mock, when}

class PublisherGuessingServiceTest {

  private val resourceDAO = mock(classOf[HibernateResourceDAO])
  private val publisherGuessingService = new PublisherGuessingService(resourceDAO, new UrlParser)

  @Test
  def shouldNotMatchIfNoMatchingPublishers {
    val possiblePublishers = Seq()

    when(resourceDAO.getAllPublishersMatchingStem("www.spammer.com", true)).thenReturn(possiblePublishers)

    assertEquals(None, publisherGuessingService.guessPublisherBasedOnUrl("http://www.spammer.com"))
  }

  @Test
  def shouldMatchIfMultipleAvailable {
    val golfCourseSite = Website(page = "http://www.wellington.govt.nz/services/berhgolf/index.html")
    val heritageInventory = Website(page = "http://www.wellington.govt.nz/services/heritage/inventory/index.html")
    val wccMainSite = Website(page = "http://www.wellington.govt.nz")

    val possiblePublishers = Seq(golfCourseSite, heritageInventory, wccMainSite)
    when(resourceDAO.getAllPublishersMatchingStem("www.wellington.govt.nz", showBroken = true)).thenReturn(possiblePublishers)

    assertEquals(Some(wccMainSite), publisherGuessingService.guessPublisherBasedOnUrl("http://www.wellington.govt.nz/news/display-item.php?id=3542"))
  }

  @Test
  def shouldMatchIfOnlyOnePossiblePublisher {
    val wellingtonista = Website(title = Some("The Wellingtonista"), page = "http://www.wellingtonista.com")
    val possiblePublishers = Seq(wellingtonista)
    when(resourceDAO.getAllPublishersMatchingStem("www.wellingtonista.com", showBroken = true)).thenReturn(possiblePublishers)

    assertEquals(Some(wellingtonista), publisherGuessingService.guessPublisherBasedOnUrl("http://www.wellingtonista.com/a-week-of-it"))
  }

  @Test
  def shouldNotMatchJustBecauseTheHostNameMatches {
    val hostedOne = Website(page = "http://homepages.paradise.net.nz/~titahi/")
    val hostedTwo = Website(page = "http://homepages.ihug.co.nz/~waicoll/")

    val possiblePublishers = Seq(hostedOne, hostedTwo)
    when(resourceDAO.getAllPublishersMatchingStem("homepages.ihug.co.nz", showBroken = true)).thenReturn(possiblePublishers)

    assertEquals(None, publisherGuessingService.guessPublisherBasedOnUrl("http://homepages.ihug.co.nz/~spammer/"))
  }

}