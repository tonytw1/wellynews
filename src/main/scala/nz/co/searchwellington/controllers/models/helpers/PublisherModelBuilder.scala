package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.controllers.models.GeotaggedNewsitemExtractor
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{PublisherArchiveLink, Tag, User, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.tagging.RelatedTagsService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

@Component class PublisherModelBuilder @Autowired()(rssUrlBuilder: RssUrlBuilder,
                                                    relatedTagsService: RelatedTagsService,
                                                    val contentRetrievalService: ContentRetrievalService,
                                                    val urlBuilder: UrlBuilder,
                                                    geotaggedNewsitemExtractor: GeotaggedNewsitemExtractor,
                                                    commonAttributesModelBuilder: CommonAttributesModelBuilder,
                                                    frontendResourceMapper: FrontendResourceMapper) extends ModelBuilder
  with CommonSizes with Pagination with ReasonableWaits {

  def isValid(request: HttpServletRequest): Boolean = {
    val tag = request.getAttribute("tag").asInstanceOf[Tag]
    val publisher = request.getAttribute("publisher").asInstanceOf[Website]
    val isPublisherPage = publisher != null && tag == null
    isPublisherPage
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {

    def populatePublisherPageModelAndView(publisher: Website, page: Int): Future[Option[ModelAndView]] = {
      val startIndex = getStartIndex(page, MAX_NEWSITEMS)

      val eventualPublisherNewsitems = contentRetrievalService.getPublisherNewsitems(publisher, MAX_NEWSITEMS, startIndex, loggedInUser)
      val eventualPublisherFeeds = contentRetrievalService.getPublisherFeeds(publisher, loggedInUser)
      val eventualFrontendWebsite = frontendResourceMapper.createFrontendResourceFrom(publisher)

      for {
        publisherNewsitems <- eventualPublisherNewsitems
        publisherFeeds <- eventualPublisherFeeds
        frontendWebsite <- eventualFrontendWebsite

      } yield {
        val mv = new ModelAndView().addObject("heading", publisher.title.getOrElse("")).
          addObject("description", publisher.title.getOrElse("") + " newsitems").
          addObject("publisher", frontendWebsite).
          addObject("location", frontendWebsite.getPlace).
          addObject("link", urlBuilder.fullyQualified(urlBuilder.getPublisherUrl(publisher)))

        val totalPublisherNewsitems = publisherNewsitems._2
        if (publisherNewsitems._1.nonEmpty) {
          mv.addObject(MAIN_CONTENT, publisherNewsitems._1.asJava)
          mv.addObject("main_heading", publisher.getTitle + " newsitems")

          def paginationLinks(page: Int): String = {
            urlBuilder.getPublisherPageUrl(publisher, page)
          }
          populatePagination(mv, startIndex, totalPublisherNewsitems, MAX_NEWSITEMS, paginationLinks)

          commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForPublisher(publisher), rssUrlBuilder.getRssUrlForPublisher(publisher))

          populateGeotaggedItems(mv, publisherNewsitems._1) // TODO This should be a seperate query
        }
        mv.addObject("feeds", publisherFeeds.asJava)

        Some(mv)
      }
    }

    val publisher = request.getAttribute("publisher").asInstanceOf[Website]
    val page = getPage(request)
    populatePublisherPageModelAndView(publisher, page)
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User]): Future[ModelMap] = {
    val publisher = request.getAttribute("publisher").asInstanceOf[Website]

    val eventualPublisherWatchlist = contentRetrievalService.getPublisherWatchlist(publisher, loggedInUser)
    val eventualPublisherArchiveLinks = contentRetrievalService.getPublisherArchiveMonths(publisher, loggedInUser)
    val eventualRelatedTagsForPublisher = relatedTagsService.getRelatedTagsForPublisher(publisher, loggedInUser)
    val eventualDiscoveredFeeds = contentRetrievalService.getDiscoveredFeedsForPublisher(publisher, MAX_NEWSITEMS)
    val eventualFrontendPublisher = frontendResourceMapper.createFrontendResourceFrom(publisher, loggedInUser) // TODO duplicated with main content

    for {
      publisherWatchlist <- eventualPublisherWatchlist
      archiveLinks <- eventualPublisherArchiveLinks
      relatedTagsForPublisher <- eventualRelatedTagsForPublisher
      discoveredFeeds <- eventualDiscoveredFeeds
      frontendPublisher <- eventualFrontendPublisher
      latestNewsitems <- latestNewsitems(loggedInUser)

    } yield {
      val mv = new ModelMap()
      val publisherArchiveLinks = archiveLinks.map { a =>
        PublisherArchiveLink(publisher = frontendPublisher, interval = a.interval, count = a.count)
      }
      mv.addAttribute("watchlist", publisherWatchlist.asJava)
      if (relatedTagsForPublisher.nonEmpty) {
        mv.addAttribute("related_tags", relatedTagsForPublisher.asJava)
      }
      if (publisherArchiveLinks.nonEmpty) {
        mv.addAttribute("publisher_archive_links", publisherArchiveLinks.asJava)
      }
      if (discoveredFeeds.nonEmpty) {
        mv.addAttribute("discovered_feeds", discoveredFeeds.asJava)
      }
      mv.addAllAttributes(latestNewsitems)
    }
  }

  def getViewName(mv: ModelAndView, loggedInUser: Option[User]): String = "publisher"

  private def populateGeotaggedItems(mv: ModelAndView, mainContent: Seq[FrontendResource]): Unit = {
    val geotaggedNewsitems = geotaggedNewsitemExtractor.extractGeotaggedItems(mainContent)
    if (geotaggedNewsitems.nonEmpty) {
      mv.addObject("geocoded", geotaggedNewsitems.asJava)
    }
  }

}
