package nz.co.searchwellington.urls

import java.util.UUID

import nz.co.searchwellington.model.frontend.{FrontendFeed, FrontendNewsitem, FrontendWebsite}
import nz.co.searchwellington.model.{SiteInformation, Tag, UrlWordsGenerator, Website}
import org.joda.time.DateTime
import org.junit.Assert.{assertEquals, assertNull}
import org.junit.{Before, Test}
import org.mockito.Mockito
import org.mockito.Mockito.mock
import uk.co.eelpieconsulting.common.geo.model.{LatLong, OsmId, OsmType, Place}

class UrlBuilderTest {

  private val SITE_URL = "http://siteurl.test"

  private val siteInformation = mock(classOf[SiteInformation])

  private val urlBuilder = new UrlBuilder(siteInformation, new UrlWordsGenerator)

  private val frontendNewsitem: FrontendNewsitem = FrontendNewsitem(id = UUID.randomUUID().toString,
    name = "Quick brown fox jumps over the lazy dog",
    date = new DateTime(2010, 10, 12, 0, 0, 0, 0).toDate,
    urlWords = "2010/oct/12/quick-brown-fox-jumps-over-lazy-dog")
  private val tag = Tag(name = "atag")

  @Before def setup(): Unit = {
    Mockito.when(siteInformation.getUrl).thenReturn(SITE_URL)
  }

  @Test
  def testTagSearchRefinementsShouldBeOnTheTagPages(): Unit = assertEquals(SITE_URL + "/atag?keywords=something", urlBuilder.getTagSearchUrl(tag, "something"))

  @Test
  def canCreatePublisherAndTagCombinerLinkBasedOnPublisherUrlWordsAndTagName(): Unit = {
    val publisher = Website(title = Some("Wellington City Council"), url_words = Some("wellington-city-council"))

    val result = urlBuilder.getPublisherCombinerUrl(publisher, tag)

    assertEquals(SITE_URL + "/wellington-city-council+atag", result)
  }

  @Test
  def useLatLongWhenBuildingUrlsToPlacesWithNoOsmId(): Unit = {
    val somewhere = new Place("Somewhere,Far away", new LatLong(3.1, 4.2), null)
    assertEquals(SITE_URL + "/geotagged?latitude=3.1&longitude=4.2", urlBuilder.getLocationUrlFor(somewhere))
  }

  @Test
  def locationsShouldBeLinkedByOSMIdIfAvailable(): Unit = {
    val somewhereWithOSMid = new Place("Somewhere,Far away", new LatLong(3.1, 4.2), new OsmId(12345, OsmType.NODE))
    assertEquals(SITE_URL + "/geotagged?osm=12345%2FNODE", urlBuilder.getLocationUrlFor(somewhereWithOSMid))
  }

  @Test
  def shouldConstructPageUrlForFrontendResourceFromResourcesUrlWords(): Unit = {
    assertNull(frontendNewsitem.getPublisherName)
    assertEquals(SITE_URL + "/2010/oct/12/quick-brown-fox-jumps-over-lazy-dog", urlBuilder.getLocalPageUrl(frontendNewsitem))
  }

  @Test
  def canGenerateFrontendPublisherPageUrl(): Unit = assertEquals(SITE_URL + "/wellington-city-council", urlBuilder.getPublisherUrl("Wellington City Council"))

  @Test
  def urlForFeedsShouldPointToOurFeedPage(): Unit = {
    val frontendFeed = FrontendFeed(id = UUID.randomUUID().toString, urlWords = "my-local-sports-team-match-reports")

    assertEquals(SITE_URL + "/feed/my-local-sports-team-match-reports", urlBuilder.getFeedUrl(frontendFeed))
  }

  @Test
  def canComposeOsmWebsiteLinkforOsmIds(): Unit = {
    val osmId = new OsmId(24724709, OsmType.WAY)
    assertEquals("http://www.openstreetmap.org/browse/way/24724709", urlBuilder.getOsmWebsiteUrl(osmId))
  }

  @Test
  def archiveLinksAreYearMonthFormatted(): Unit = {
    val feb = new DateTime(2020, 2, 12, 0, 0, 0)

    val link = urlBuilder.getArchiveLinkUrl(feb.toDate)

    assertEquals("/archive/2020-feb", link)
  }

  @Test
  def publisherArchiveLinksAreYearMonthFormatted(): Unit = {
    val publisher = FrontendWebsite(id = "123", urlWords = "a-publisher")
    val feb = new DateTime(2020, 2, 12, 0, 0, 0)

    val link = urlBuilder.getPublisherArchiveLinkUrl(publisher, feb.toDate)

    assertEquals("/a-publisher/2020-feb", link)
  }

}
