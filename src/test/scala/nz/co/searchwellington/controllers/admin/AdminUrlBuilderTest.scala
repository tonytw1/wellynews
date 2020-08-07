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

  private val frontendFeed: FrontendFeed = new FrontendFeed(id = "124")
  private val frontendWebsite = FrontendWebsite(id = "123", name = "My local sports team", urlWords = "my-local-sports-team")

  private val adminUrlBuilder = new AdminUrlBuilder(new UrlBuilder(new SiteInformation(), new UrlWordsGenerator), "", "")

  @Test
  def canConstructEditUrlForFrontendWebsite(): Unit = {
    assertEquals("/edit-website/123", adminUrlBuilder.getResourceEditUrl(frontendWebsite))
  }

  @Test
  def canBuildEditUrlForNewsitems(): Unit = {
    val frontendNewsitem = FrontendNewsitem(id = "123", name = "A news item", publisher = None,
      date = new DateTime(2011, 5, 20, 0, 0, 0, 0).toDate)
    assertEquals("/edit-newsitem/123", adminUrlBuilder.getResourceEditUrl(frontendNewsitem))
  }

  @Test
  def canBuildEditUrlForFeeds(): Unit = {
    assertEquals("/edit-feed/124", adminUrlBuilder.getResourceEditUrl(frontendFeed))
  }

  @Test
  def canConstructEditUrlForFrontendFeed(): Unit = assertEquals("/edit-feed/124", adminUrlBuilder.getResourceEditUrl(frontendFeed))

  @Test
  def canConstructDeleteUrlForFrontendResource(): Unit = assertEquals("/delete?resource=123", adminUrlBuilder.getResourceDeleteUrl(frontendWebsite))

  @Test
  def canConstructCheckUrlForFrontendResource(): Unit = assertEquals("/admin/linkchecker/add?resource=123", adminUrlBuilder.getResourceCheckUrl(frontendWebsite))

  @Test
  def canConstructViewSnapshotUrlForFrontendResource(): Unit = assertEquals("/my-local-sports-team/viewsnapshot", adminUrlBuilder.getViewSnapshotUrl(frontendWebsite))

  //  @Test
  //  def canConstructAutoGatherUrlForPublisher(): Unit = assertEquals("http://somesite.local/my-local-sports-team/gather", adminUrlBuilder.getPublisherAutoGatherUrl(frontendWebsite))
}
