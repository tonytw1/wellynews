package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.controllers.admin.AdminUrlBuilder
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.frontend.{FrontendNewsitem, FrontendResource}
import nz.co.searchwellington.model.geo.Geocode
import nz.co.searchwellington.permissions.EditPermissionService
import nz.co.searchwellington.repositories.{ContentRetrievalService, TagDAO}
import nz.co.searchwellington.tagging.RelatedTagsService
import nz.co.searchwellington.urls.UrlBuilder
import org.joda.time.{DateTime, DateTimeZone, Interval}
import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse, assertNotNull, assertTrue}
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.ui.ModelMap
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.dates.DateFormatter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

class TagModelBuilderTest extends ReasonableWaits with ContentFields {

  private val siteInformation = new SiteInformation("Area name", "", "", "", "", sitename = "Site name")
  private val urlBuilder = new UrlBuilder(siteInformation, new UrlWordsGenerator(new DateFormatter(DateTimeZone.UTC)))
  private val rssUrlBuilder = new RssUrlBuilder(siteInformation)

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val relatedTagsService = mock(classOf[RelatedTagsService])
  private val commonAttributesModelBuilder = new CommonAttributesModelBuilder()
  private val tagDAO = mock(classOf[TagDAO])
  private val editPermissionService = mock(classOf[EditPermissionService])
  private val adminUrlBuilder = mock(classOf[AdminUrlBuilder])

  private val newsitem1 = mock(classOf[FrontendResource])
  private val newsitem2 = mock(classOf[FrontendResource])

  private val TAG_DISPLAY_NAME = "Penguins"

  private val parentTag = Tag(display_name = "Parent")
  private val tag = Tag(parent = Some(parentTag._id), display_name = TAG_DISPLAY_NAME, name = "penguins")

  private val loggedInUser = None

  private val noNewsitems = (Seq.empty, 0L)

  val request = new MockHttpServletRequest()

  private implicit val currentSpan: Span = Span.current()

  private val modelBuilder = new TagModelBuilder(rssUrlBuilder, urlBuilder, relatedTagsService,
    contentRetrievalService, commonAttributesModelBuilder, tagDAO, editPermissionService, adminUrlBuilder)

  {
    when(tagDAO.loadTagsByParent(tag._id)).thenReturn(Future.successful(List.empty))
    when(tagDAO.loadTagByObjectId(parentTag._id)).thenReturn(Future.successful(Some(parentTag)))
  }

  @Test
  def isNotValidIfNotTagsAreOnTheRequest(): Unit = {
    assertFalse(modelBuilder.isValid(request))
  }

  @Test
  def isValidIsOneTagIsOnTheRequest(): Unit = {
    request.setAttribute("tags", Seq(tag))
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def isNotValidIfMoreThanOneTagIsOnTheRequest(): Unit = {
    request.setAttribute("tags", Seq(tag, tag))
    assertFalse(modelBuilder.isValid(request))
  }

  @Test
  def tagPageHeadingShouldBeTheTagDisplayName(): Unit = {
    request.setAttribute("tags", Seq(tag))
    when(contentRetrievalService.getTaggedNewsitems(tag, 30, loggedInUser)).thenReturn(Future.successful(noNewsitems))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(TAG_DISPLAY_NAME, mv.get("heading"))
  }

  @Test
  def mainContentShouldBeTagNewsitems(): Unit = {
    request.setAttribute("tags", Seq(tag))
    val tagNewsitems = Seq(newsitem1, newsitem2)
    when(contentRetrievalService.getTaggedNewsitems(tag, 30, loggedInUser)).thenReturn(Future.successful((tagNewsitems, tagNewsitems.size.toLong)))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(tagNewsitems.asJava, mv.get(MAIN_CONTENT))
    assertEquals("Penguins related newsitems", mv.get("main_heading"))
  }

  @Test
  def shouldIncludeTagParent(): Unit = {
    request.setAttribute("tags", Seq(tag))
    when(contentRetrievalService.getTaggedNewsitems(tag, 30, loggedInUser)).thenReturn(Future.successful(noNewsitems))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(parentTag, mv.get("parent"))
  }

  @Test
  def monthPaginationShouldBePopulatedFromDateOfFirstOverFetchedMainContentItem(): Unit = {
    val request = new MockHttpServletRequest
    request.setAttribute("tags", Seq(tag))

    val maxedOutTagNewsitems = Range(1, 20).map { i =>
      val d = new DateTime(2022, 1, 30, 0, 0, 0)
      FrontendNewsitem(id = i.toString, date = d.minusDays(i).toDate)
    }

    when(contentRetrievalService.getTaggedNewsitems(tag, 30, loggedInUser)).
      thenReturn(Future.successful((maxedOutTagNewsitems, 400L)))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertNotNull(mv.get("more"))
    val moreLink = mv.get("more").asInstanceOf[String]
    assertEquals("/penguins/2022-jan", moreLink)
  }

  @Test
  def shouldIncludeTagMonthArchiveLinksInExtraContent(): Unit = {
    request.setAttribute("tags", Seq(tag))
    when(contentRetrievalService.getGeotaggedNewsitemsForTag(tag, 30, loggedInUser = None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getTaggedWebsites(tag, 500, loggedInUser = None)).thenReturn(Future.successful(Seq.empty))
    when(relatedTagsService.getRelatedTagsForTag(tag, 8, None)).thenReturn(Future.successful(Seq.empty))
    when(relatedTagsService.getRelatedPublishersForTag(tag, 8, None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getTagWatchlist(tag, None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getTaggedFeeds(tag, None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getLatestNewsitems(5, loggedInUser = None)).thenReturn(Future.successful(Seq.empty))

    val january = new DateTime(2021, 1, 1, 0,0, 0, 0)
    val start = new DateTime(january, DateTimeZone.UTC)
    val a = ArchiveLink(count = Some(12L), interval = new Interval(start, start.plusMonths(1)))
    val b = ArchiveLink(count = Some(24L), interval = new Interval(start.plusMonths(1), start.plusMonths(2)))
    val tagArchiveMonths = Seq(a, b)

    when(contentRetrievalService.getTagArchiveMonths(tag, loggedInUser = None)).thenReturn(Future.successful(tagArchiveMonths))

    val extras = Await.result(modelBuilder.populateExtraModelContent(request, None), TenSeconds)

    val archiveLinksOnModel = extras.get("archive_links").asInstanceOf[java.util.List[TagArchiveLink]]
    assertNotNull(archiveLinksOnModel)
    assertEquals(2, archiveLinksOnModel.size())
    assertEquals(Some(12L), archiveLinksOnModel.get(0).count)
    assertEquals(tag, archiveLinksOnModel.get(0).tag)
  }

  @Test
  def tagPageExtras(): Unit = {
    request.setAttribute("tags", Seq(tag))
    val geotagged = Seq(FrontendNewsitem(id = "123", geocode = Some(Geocode(address = Some("Somewhere")))))
    when(contentRetrievalService.getGeotaggedNewsitemsForTag(tag, 30, loggedInUser = None)).thenReturn(Future.successful(geotagged))
    when(contentRetrievalService.getTaggedWebsites(tag, 500, loggedInUser = None)).thenReturn(Future.successful(Seq.empty))
    when(relatedTagsService.getRelatedTagsForTag(tag, 8, None)).thenReturn(Future.successful(Seq.empty))
    when(relatedTagsService.getRelatedPublishersForTag(tag, 8, None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getTagWatchlist(tag, None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getTaggedFeeds(tag, None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getLatestNewsitems(5, loggedInUser = None)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getTagArchiveMonths(tag, loggedInUser)).thenReturn(Future.successful(Seq.empty))

    val extras = Await.result(modelBuilder.populateExtraModelContent(request, None), TenSeconds)

    assertEquals(geotagged.asJava, extras.get("geocoded"))
  }

  @Test
  def shouldSetTagRssFeed(): Unit = {
    val newsitems = Seq(FrontendNewsitem(id = "123", geocode = Some(Geocode(address = Some("Somewhere")))))
    val mv = new ModelMap().addAttribute("tag", tag).addAttribute(MAIN_CONTENT, newsitems.asJava)

    val withRss = modelBuilder.setRss(mv)

    assertEquals("Site name - Penguins", withRss.get("rss_title"))
    assertEquals("/penguins/rss", withRss.get("rss_url"))
  }

}
