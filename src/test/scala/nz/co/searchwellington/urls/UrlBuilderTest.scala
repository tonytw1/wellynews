package nz.co.searchwellington.urls

import java.util.UUID
import nz.co.searchwellington.model.frontend.{FrontendFeed, FrontendNewsitem, FrontendWebsite}
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.geo.OsmId
import org.joda.time.{DateTime, DateTimeZone, Interval}
import org.junit.jupiter.api.Assertions.{assertEquals, assertNull}
import org.junit.jupiter.api.Test
import uk.co.eelpieconsulting.common.dates.DateFormatter
import uk.co.eelpieconsulting.common.geo.model.{LatLong, OsmType, Place}

class UrlBuilderTest {

  private val frontendNewsitem = FrontendNewsitem(id = UUID.randomUUID().toString,
    name = "Quick brown fox jumps over the lazy dog",
    date = new DateTime(2010, 10, 12, 0, 0, 0, 0).toDate,
    urlWords = "2010/oct/12/quick-brown-fox-jumps-over-lazy-dog")

  private val tag = Tag(name = "atag")

  private val urlBuilder = new UrlBuilder(new SiteInformation(url = "https://wellynews.local"), new UrlWordsGenerator(new DateFormatter(DateTimeZone.UTC)))

  @Test
  def shouldBuildPublisherKeywordSearchUrl(): Unit = {
    val publisher = Website(title = "Wellington City Council", url_words = Some("wellington-city-council"))
    val url = urlBuilder.getPublisherSearchUrl(publisher, "something")
    assertEquals("/search?q=something&publisher=wellington-city-council", url)
  }

  @Test
  def shouldBuildTagKeywordSearchUrl(): Unit = {
    val tag = Tag(name = "transport")
    val url = urlBuilder.getTagSearchUrl(tag, keywords = "something")
    assertEquals("/search?q=something&tag=transport", url)
  }

  @Test
  def shouldCreatePublisherAndTagCombinerLinkBasedOnPublisherUrlWordsAndTagName(): Unit = {
    val publisher = Website(title = "Wellington City Council", url_words = Some("wellington-city-council"))

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
  def urlForFeedsShouldPointToOurFeedPage(): Unit = {
    val frontendFeed = FrontendFeed(id = UUID.randomUUID().toString, urlWords = "my-local-sports-team-match-reports")
    assertEquals("/feed/my-local-sports-team-match-reports", urlBuilder.getFeedUrl(frontendFeed))
  }

  @Test
  def shouldComposeOsmWebsiteLinkforOsmIds(): Unit = {
    val osmId = OsmId(24724709, OsmType.WAY.toString)
    assertEquals("http://www.openstreetmap.org/browse/way/24724709", urlBuilder.getOsmWebsiteUrl(osmId))
  }

  @Test
  def archiveLinksAreYearMonthFormatted(): Unit = {
    val feb = new DateTime(2020, 2, 12, 0, 0, 0)
    val monthOfFeb = new Interval(feb, feb.plusMonths(1))

    val link = urlBuilder.getArchiveLinkUrl(ArchiveLink(monthOfFeb, Some(1)))

    assertEquals("/archive/2020-feb", link)
  }

  @Test
  def publisherArchiveLinksAreYearMonthFormatted(): Unit = {
    val publisher = FrontendWebsite(id = "123", urlWords = "a-publisher")

    val feb = new DateTime(2020, 2, 12, 0, 0, 0)
    val monthOfFeb = new Interval(feb, feb.plusMonths(1))

    val link = urlBuilder.getPublisherArchiveLinkUrl(PublisherArchiveLink(publisher, monthOfFeb, Some(3)))

    assertEquals("/a-publisher/2020-feb", link)
  }

  @Test
  def shouldBuildFullyQualifiedUrlForTwitterSigninCallback(): Unit = {
    assertEquals("https://wellynews.local/twitter/callback", urlBuilder.getTwitterCallbackUrl)
  }

  @Test
  def taggedAsLinksShouldClickThroughToNewsitemPage(): Unit = {
    val taggedAsLink = urlBuilder.getLocalPageUrl(frontendNewsitem)

    assertEquals("/newsitem/" + frontendNewsitem.getId, taggedAsLink)
  }

  @Test
  def taggedAsLinksShouldClickThroughToPublisherPage(): Unit = {
    val publisher = FrontendWebsite(id = "123", urlWords = "a-publisher")

    val taggedAsLink = urlBuilder.getLocalPageUrl(publisher)

    assertEquals("/a-publisher", taggedAsLink)
  }

}
