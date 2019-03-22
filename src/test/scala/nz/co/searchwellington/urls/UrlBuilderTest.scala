package nz.co.searchwellington.urls

import java.util.UUID

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import nz.co.searchwellington.model.{SiteInformation, Tag, UrlWordsGenerator}
import nz.co.searchwellington.model.frontend.FrontendFeed
import nz.co.searchwellington.model.frontend.FrontendNewsitem
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import uk.co.eelpieconsulting.common.geo.model.LatLong
import uk.co.eelpieconsulting.common.geo.model.OsmId
import uk.co.eelpieconsulting.common.geo.model.OsmType
import uk.co.eelpieconsulting.common.geo.model.Place

object UrlBuilderTest {
  private val SITE_URL = "http://siteurl.test"
}

class UrlBuilderTest {
  @Mock private[urls] val siteInformation: SiteInformation = null
  private var urlBuilder: UrlBuilder = null
  private var frontendNewsitem: FrontendNewsitem = null
  private var tag: Tag = null

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)
    Mockito.when(siteInformation.getUrl).thenReturn(UrlBuilderTest.SITE_URL)
    urlBuilder = new UrlBuilder(siteInformation, new UrlWordsGenerator)
    frontendNewsitem = FrontendNewsitem(name = "Quick brown fox jumps over the lazy dog",
      date = new DateTime(2010, 10, 12, 0, 0, 0, 0).toDate,
      urlWords = "2010/oct/12/quick-brown-fox-jumps-over-lazy-dog")

    tag = new Tag(id = UUID.randomUUID().toString)
    tag.setName("atag")
  }

  @Test
  @throws[Exception]
  def testTagSearchRefinementsShouldBeOnTheTagPages(): Unit = assertEquals(UrlBuilderTest.SITE_URL + "/atag?keywords=something", urlBuilder.getTagSearchUrl(tag, "something"))

  @Test
  @throws[Exception]
  def canCreatePublisherAndTagCombinerLinkBasedOnPublisherUrlWordsAndTagName(): Unit = assertEquals(UrlBuilderTest.SITE_URL + "/wellington-city-council+atag", urlBuilder.getPublisherCombinerUrl("Wellington City Council", tag))

  @Test
  @throws[Exception]
  def useLatLongWhenBuildingUrlsToPlacesWithNoOsmId(): Unit = {
    val somewhere = new Place("Somewhere,Far away", new LatLong(3.1, 4.2), null)
    assertEquals(UrlBuilderTest.SITE_URL + "/geotagged?latitude=3.1&longitude=4.2", urlBuilder.getLocationUrlFor(somewhere))
  }

  @Test
  @throws[Exception]
  def locationsShouldBeLinkedByOSMIdIfAvailable(): Unit = {
    val somewhereWithOSMid = new Place("Somewhere,Far away", new LatLong(3.1, 4.2), new OsmId(12345, OsmType.NODE))
    assertEquals(UrlBuilderTest.SITE_URL + "/geotagged?osm=12345%2FNODE", urlBuilder.getLocationUrlFor(somewhereWithOSMid))
  }


  @Test
  @throws[Exception]
  def shouldConstructPageUrlForFrontendResourceFromResourcesUrlWords(): Unit = {
    assertNull(frontendNewsitem.getPublisherName)
    assertEquals(UrlBuilderTest.SITE_URL + "/2010/oct/12/quick-brown-fox-jumps-over-lazy-dog", urlBuilder.getLocalPageUrl(frontendNewsitem))
  }

  @Test
  @throws[Exception]
  def canGenerateFrontendPublisherPageUrl(): Unit = assertEquals(UrlBuilderTest.SITE_URL + "/wellington-city-council", urlBuilder.getPublisherUrl("Wellington City Council"))


  @Test
  @throws[Exception]
  def urlForFeedsShouldPointToOurFeedPage(): Unit = {
    val frontendFeed = new FrontendFeed(id = UUID.randomUUID().toString, urlWords = "my-local-sports-team-match-reports")

    assertEquals(UrlBuilderTest.SITE_URL + "/feed/my-local-sports-team-match-reports", urlBuilder.getFeedUrl(frontendFeed))
  }

  @Test
  @throws[Exception]
  def canComposeOsmWebsiteLinkforOsmIds(): Unit = {
    val osmId = new OsmId(24724709, OsmType.WAY)
    assertEquals("http://www.openstreetmap.org/browse/way/24724709", urlBuilder.getOsmWebsiteUrl(osmId))
  }

}
