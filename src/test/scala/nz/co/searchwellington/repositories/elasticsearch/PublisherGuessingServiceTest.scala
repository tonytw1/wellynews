package nz.co.searchwellington.repositories.elasticsearch

import junit.framework.TestCase
import nz.co.searchwellington.model.{Website, WebsiteImpl}
import nz.co.searchwellington.repositories.HibernateResourceDAO
import nz.co.searchwellington.urls.UrlParser
import org.junit.Before
import org.mockito.Mockito.when
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.junit.Assert._

class PublisherGuessingServiceTest extends TestCase {
  @Mock val resourceDAO: HibernateResourceDAO = null
  private var service: PublisherGuessingService = null

  @Before
  @throws[Exception]
  override def setUp {
    MockitoAnnotations.initMocks(this)
    service = new PublisherGuessingService(resourceDAO, new UrlParser)
  }

  @throws[Exception]
  def testShouldNotMatchIfNoMatchingPublishers {
    val possiblePublishers = Seq()

    when(resourceDAO.getAllPublishersMatchingStem("www.spammer.com", true)).thenReturn(possiblePublishers)

    assertEquals(None, service.guessPublisherBasedOnUrl("http://www.spammer.com"))
  }

  @throws[Exception]
  def testShouldMatchIfMultipleAvailable {
    val golfCourseSite = new WebsiteImpl
    golfCourseSite.setUrl("http://www.wellington.govt.nz/services/berhgolf/index.html")

    val heritageInventory = new WebsiteImpl
    heritageInventory.setUrl("http://www.wellington.govt.nz/services/heritage/inventory/index.html")

    val wccMainSite = new WebsiteImpl
    wccMainSite.setUrl("http://www.wellington.govt.nz")

    val possiblePublishers = Seq(golfCourseSite, heritageInventory, wccMainSite)
    when(resourceDAO.getAllPublishersMatchingStem("www.wellington.govt.nz", true)).thenReturn(possiblePublishers)

    assertEquals(Some(wccMainSite), service.guessPublisherBasedOnUrl("http://www.wellington.govt.nz/news/display-item.php?id=3542"))
  }

  @throws[Exception]
  def testShouldMatchIfOnlyOnePossiblePublisher {
    val wellingtonista: Website = new WebsiteImpl
    wellingtonista.setName("The Wellingtonista")
    wellingtonista.setUrl("http://www.wellingtonista.com")

    val possiblePublishers = Seq(wellingtonista)
    when(resourceDAO.getAllPublishersMatchingStem("www.wellingtonista.com", true)).thenReturn(possiblePublishers)

    assertEquals(Some(wellingtonista), service.guessPublisherBasedOnUrl("http://www.wellingtonista.com/a-week-of-it"))
  }

  @throws[Exception]
  def testShouldNotMatchJustBecauseTheHostNameMatches {
    val hostedOne = new WebsiteImpl
    hostedOne.setUrl("http://homepages.paradise.net.nz/~titahi/")
    val hostedTwo = new WebsiteImpl
    hostedTwo.setUrl("http://homepages.ihug.co.nz/~waicoll/")

    val possiblePublishers = Seq(hostedOne, hostedTwo)
    when(resourceDAO.getAllPublishersMatchingStem("homepages.paradise.net.nz", true)).thenReturn(possiblePublishers)

    assertEquals(None, service.guessPublisherBasedOnUrl("http://homepages.ihug.co.nz/~spammer/"))
  }

}