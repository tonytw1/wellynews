package nz.co.searchwellington.urls

import java.util.UUID

import nz.co.searchwellington.model.frontend.{FrontendFeed, FrontendNewsitem, FrontendWebsite}
import nz.co.searchwellington.model._
import org.joda.time.{DateTime, Interval}
import org.junit.Assert.{assertEquals, assertNull}
import org.junit.Test
import uk.co.eelpieconsulting.common.geo.model.{LatLong, OsmType, Place}

class UrlBuilderTest {

  private val frontendNewsitem = FrontendNewsitem(id = UUID.randomUUID().toString,
    name = "Quick brown fox jumps over the lazy dog",
    date = new DateTime(2010, 10, 12, 0, 0, 0, 0).toDate,
    urlWords = "2010/oct/12/quick-brown-fox-jumps-over-lazy-dog")

  private val tag = Tag(name = "atag")

  private val urlBuilder = new UrlBuilder(new SiteInformation(url = "https://wellynews.local"), new UrlWordsGenerator)

  @Test
  def testTagSearchRefinementsShouldBeOnTheTagPages(): Unit = assertEquals("/atag?keywords=something", urlBuilder.getTagSearchUrl(tag, "something"))

  @Test
  def canCreatePublisherAndTagCombinerLinkBasedOnPublisherUrlWordsAndTagName(): Unit = {
    val publisher = Website(title = Some("Wellington City Council"), url_words = Some("wellington-city-council"))

    val result = urlBuilder.getPublisherCombinerUrl(publisher, tag)

    assertEquals("/wellington-city-council+atag", result)
  }

  @Test
  def useLatLongWhenBuildingUrlsToPlacesWithNoOsmId(): Unit = {
    val somewhere = new Place("Somewhere,Far away", new LatLong(3.1, 4.2), null)
    assertEquals("/geotagged?latitude=3.1&longitude=4.2", urlBuilder.getLocationUrlFor(somewhere))
  }

  @Test
  def locationsShouldBeLinkedByOSMIdIfAvailable(): Unit = {
    val somewhereWithOSMid = new Place("Somewhere,Far away", new LatLong(3.1, 4.2),
      new uk.co.eelpieconsulting.common.geo.model.OsmId(12345, OsmType.NODE))
    assertEquals("/geotagged?osm=12345%2FNODE", urlBuilder.getLocationUrlFor(somewhereWithOSMid))
  }

  @Test
  def shouldConstructPageUrlForFrontendResourceFromResourcesUrlWords(): Unit = {
    assertNull(frontendNewsitem.getPublisherName)
    assertEquals("/newsitem/" + frontendNewsitem.id, urlBuilder.getLocalPageUrl(frontendNewsitem))
  }

  @Test
  def urlForFeedsShouldPointTaggingFeedPage(): Unit = {
    val frontendFeed = FrontendFeed(id = UUID.randomUUID().toString, urlWords = "my-local-sports-team-match-reports")
    assertEquals("/feed/my-local-sports-team-match-reports", urlBuilder.getLocalPageUrl(frontendFeed))
  }

  @Test
  def canGenerateFrontendPublisherPageUrl(): Unit = assertEquals("/wellington-city-council", urlBuilder.getPublisherUrl("Wellington City Council"))

  @Test
  def urlForFeedsShouldPointToOurFeedPage(): Unit = {
    val frontendFeed = FrontendFeed(id = UUID.randomUUID().toString, urlWords = "my-local-sports-team-match-reports")
    assertEquals("/feed/my-local-sports-team-match-reports", urlBuilder.getFeedUrl(frontendFeed))
  }

  @Test
  def canComposeOsmWebsiteLinkforOsmIds(): Unit = {
    val osmId = new OsmId(24724709, OsmType.WAY.toString)
    assertEquals("http://www.openstreetmap.org/browse/way/24724709", urlBuilder.getOsmWebsiteUrl(osmId))
  }

  @Test
  def archiveLinksAreYearMonthFormatted(): Unit = {
    val feb = new DateTime(2020, 2, 12, 0, 0, 0)
    val monthOfFeb = new Interval(feb, feb.plusMonths(1))

    val link = urlBuilder.getArchiveLinkUrl(new ArchiveLink(monthOfFeb, 1))

    assertEquals("/archive/2020-feb", link)
  }

  @Test
  def publisherArchiveLinksAreYearMonthFormatted(): Unit = {
    val publisher = FrontendWebsite(id = "123", urlWords = "a-publisher")

    val feb = new DateTime(2020, 2, 12, 0, 0, 0)
    val monthOfFeb = new Interval(feb, feb.plusMonths(1))

    val link = urlBuilder.getPublisherArchiveLinkUrl(PublisherArchiveLink(publisher, monthOfFeb, 3))

    assertEquals("/a-publisher/2020-feb", link)
  }

  @Test
  def shouldBuildFullyQualifiedUrlForTwitterSigninCallback(): Unit = {
    assertEquals("https://wellynews.local/twitter/callback", urlBuilder.getTwitterCallbackUrl)
  }

}
