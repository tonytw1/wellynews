package nz.co.searchwellington.controllers.admin

import org.junit.Assert.assertEquals
import org.mockito.Mockito.when
import nz.co.searchwellington.model.SiteInformation
import nz.co.searchwellington.model.UrlWordsGenerator
import nz.co.searchwellington.model.frontend.FrontendFeed
import nz.co.searchwellington.model.frontend.FrontendNewsitem
import nz.co.searchwellington.model.frontend.FrontendWebsite
import nz.co.searchwellington.urls.UrlBuilder
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class AdminUrlBuilderTest {

  private val SITE_URL = "http://somesite.local"

  @Mock val siteInformation: SiteInformation = null
  private var frontendWebsite: FrontendWebsite= null
  private var frontendFeed: FrontendFeed = null
  private var frontendNewsitem: FrontendNewsitem = null
  private var adminUrlBuilder: AdminUrlBuilder = null

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)
    when(siteInformation.getUrl).thenReturn(SITE_URL)
    adminUrlBuilder = new AdminUrlBuilder(siteInformation, new UrlBuilder(siteInformation, new UrlWordsGenerator))
    frontendWebsite = FrontendWebsite(id = 123, name = "My local sports team", urlWords = "my-local-sports-team")

    frontendNewsitem = FrontendNewsitem(id = 123, name = "A news item", publisherName = "My local sports team",
      date = new DateTime(2011, 5, 20, 0, 0, 0, 0).toDate)

    frontendFeed = new FrontendFeed(id = 124)
    // frontendFeed.setUrlWords("my-local-sports-team-news")
  }

  @Test
  @throws[Exception]
  def canConstructEditUrlForFrontendWebsite(): Unit = assertEquals("http://somesite.local/edit?resource=123", adminUrlBuilder.getResourceEditUrl(frontendWebsite))

  @Test
  @throws[Exception]
  def canBuildEditUrlForNewsitems(): Unit = { //assertEquals("http://somesite.local/my-local-sports-team/2011/may/20/a-news-item/edit", adminUrlBuilder.getResourceEditUrl(frontendNewsitem));
    assertEquals("http://somesite.local/edit?resource=123", adminUrlBuilder.getResourceEditUrl(frontendNewsitem))
  }

  @Test
  @throws[Exception]
  def canConstructEditUrlForFrontendFeed(): Unit = assertEquals("http://somesite.local/edit?resource=124", adminUrlBuilder.getResourceEditUrl(frontendFeed))

  @Test
  @throws[Exception]
  def canConstructDeleteUrlForFrontendResource(): Unit = assertEquals("http://somesite.local/delete?resource=123", adminUrlBuilder.getResourceDeleteUrl(frontendWebsite))

  @Test
  @throws[Exception]
  def canConstructCheckUrlForFrontendResource(): Unit = assertEquals("http://somesite.local/admin/linkchecker/add?resource=123", adminUrlBuilder.getResourceCheckUrl(frontendWebsite))

  @Test
  @throws[Exception]
  def canConstructViewSnapshotUrlForFrontendResource(): Unit = assertEquals("http://somesite.local/my-local-sports-team/viewsnapshot", adminUrlBuilder.getViewSnapshotUrl(frontendWebsite))

  @Test
  @throws[Exception]
  def canConstructAutoGatherUrlForPublisher(): Unit = assertEquals("http://somesite.local/my-local-sports-team/gather", adminUrlBuilder.getPublisherAutoGatherUrl(frontendWebsite))
}
