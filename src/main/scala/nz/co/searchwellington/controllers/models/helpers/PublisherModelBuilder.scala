package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.controllers.admin.AdminUrlBuilder
import nz.co.searchwellington.filters.attributesetters.PublisherPageAttributeSetter
import nz.co.searchwellington.model.frontend.{Action, FrontendResource, FrontendWebsite}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{PublisherArchiveLink, Tag, User, Website}
import nz.co.searchwellington.permissions.EditPermissionService
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.tagging.RelatedTagsService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Component class PublisherModelBuilder @Autowired()(rssUrlBuilder: RssUrlBuilder,
                                                    relatedTagsService: RelatedTagsService,
                                                    val contentRetrievalService: ContentRetrievalService,
                                                    val urlBuilder: UrlBuilder,
                                                    commonAttributesModelBuilder: CommonAttributesModelBuilder,
                                                    frontendResourceMapper: FrontendResourceMapper,
                                                    editPermissionService: EditPermissionService,
                                                    adminUrlBuilder: AdminUrlBuilder) extends ModelBuilder
  with CommonSizes with ReasonableWaits with ArchiveMonths {

  def isValid(request: HttpServletRequest): Boolean = {
    val tag = request.getAttribute("tag").asInstanceOf[Tag]
    val publisher = request.getAttribute(PublisherPageAttributeSetter.PUBLISHER_ATTRIBUTE).asInstanceOf[Website]
    val isPublisherPage = publisher != null && tag == null
    isPublisherPage
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]] = {

    def populatePublisherPageModelAndView(publisher: Website): Future[Option[ModelMap]] = {
      val eventualPublisherNewsitems = contentRetrievalService.getPublisherNewsitems(publisher, MAX_NEWSITEMS, loggedInUser)
      val eventualPublisherFeeds = contentRetrievalService.getPublisherFeeds(publisher, loggedInUser)
      val eventualFrontendWebsite = frontendResourceMapper.createFrontendResourceFrom(publisher)
      val eventualGeotaggedNewsitems = contentRetrievalService.getGeotaggedNewsitemsForPublisher(publisher, MAX_NUMBER_OF_GEOTAGGED_TO_SHOW, loggedInUser = loggedInUser)

      for {
        publisherNewsitemsAndTotalCount: (Seq[FrontendResource], Long) <- eventualPublisherNewsitems
        (newsitems, totalNewsitems) = publisherNewsitemsAndTotalCount
        publisherFeeds <- eventualPublisherFeeds
        frontendWebsite <- eventualFrontendWebsite
        geotaggedNewsitems <- eventualGeotaggedNewsitems

      } yield {
        val mv = new ModelMap().
          addAttribute("heading", publisher.title).
          addAttribute("description", publisher.title + " newsitems").
          addAttribute("publisher", frontendWebsite).
          addAttribute("location", frontendWebsite.getGeocode).
          addAttribute("link", urlBuilder.fullyQualified(urlBuilder.getPublisherUrl(publisher)))

        if (newsitems.nonEmpty) {
          mv.addAttribute(MAIN_CONTENT, newsitems.asJava)
          mv.addAttribute("main_heading", publisher.getTitle + " newsitems")

          if (totalNewsitems > MAX_NEWSITEMS) {
            val monthToLinkToForMore = monthOfLastItem(newsitems) // TODO this is a slight off by one.
            monthToLinkToForMore.foreach { i =>
              val moreLink = PublisherArchiveLink(publisher = frontendWebsite, interval = i, count = None)
              mv.addAttribute("more", urlBuilder.getArchiveLinkUrl(moreLink))
            }
          }
          if (geotaggedNewsitems.nonEmpty) {
            mv.addAttribute("geocoded", geotaggedNewsitems.asJava)
          }
          commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForPublisher(publisher), rssUrlBuilder.getRssUrlForPublisher(publisher))
        }
        mv.addAttribute("feeds", publisherFeeds.asJava)

        if (editPermissionService.canEdit(publisher, loggedInUser)) {
          val actions = Seq(
            Action("Edit", adminUrlBuilder.getResourceEditUrl(publisher)),
            Action("Delete", adminUrlBuilder.getResourceDeleteUrl(frontendWebsite)),
            Action("Gather", adminUrlBuilder.getPublisherAutoGatherUrl(frontendWebsite.asInstanceOf[FrontendWebsite])),
            Action("New feed", urlBuilder.getNewFeedForPublisherUrl(frontendWebsite.asInstanceOf[FrontendWebsite])),
          )
          mv.addAttribute("actions", actions.asJava)
        }
        Some(mv)
      }
    }

    val publisher = request.getAttribute(PublisherPageAttributeSetter.PUBLISHER_ATTRIBUTE).asInstanceOf[Website]
    populatePublisherPageModelAndView(publisher)
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[ModelMap] = {
    val publisher = request.getAttribute(PublisherPageAttributeSetter.PUBLISHER_ATTRIBUTE).asInstanceOf[Website]

    val eventualPublisherWatchlist = contentRetrievalService.getPublisherWatchlist(publisher, loggedInUser)
    val eventualPublisherArchiveLinks = contentRetrievalService.getPublisherArchiveMonths(publisher, loggedInUser)
    val eventualRelatedTagsForPublisher = relatedTagsService.getRelatedTagsForPublisher(publisher, loggedInUser)
    val eventualDiscoveredFeeds = contentRetrievalService.getDiscoveredFeedsForPublisher(publisher)
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
        mv.addAttribute("archive_links", publisherArchiveLinks.asJava)
      }
      if (discoveredFeeds.nonEmpty) {
        mv.addAttribute("discovered_feeds", discoveredFeeds.asJava)
      }
      mv.addAllAttributes(latestNewsitems)
    }
  }

  def getViewName(mv: ModelMap, loggedInUser: Option[User]): String = "publisher"

}
