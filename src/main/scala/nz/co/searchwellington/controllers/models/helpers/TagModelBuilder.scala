package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.admin.AdminUrlBuilder
import nz.co.searchwellington.controllers.{CommonModelObjectsService, RssUrlBuilder}
import nz.co.searchwellington.model.frontend.{Action, FrontendResource}
import nz.co.searchwellington.model.{Resource, Tag, TagArchiveLink, User}
import nz.co.searchwellington.permissions.EditPermissionService
import nz.co.searchwellington.repositories.{ContentRetrievalService, TagDAO}
import nz.co.searchwellington.tagging.RelatedTagsService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap

import java.util
import javax.servlet.http.HttpServletRequest
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Component class TagModelBuilder @Autowired()(rssUrlBuilder: RssUrlBuilder, val urlBuilder: UrlBuilder,
                                              relatedTagsService: RelatedTagsService,
                                              val contentRetrievalService: ContentRetrievalService,
                                              commonAttributesModelBuilder: CommonAttributesModelBuilder,
                                              tagDAO: TagDAO,
                                              editPermissionService: EditPermissionService,
                                              adminUrlBuilder: AdminUrlBuilder) extends ModelBuilder
  with CommonSizes with ReasonableWaits with CommonModelObjectsService with ArchiveMonths {

  private val TAG = "tag"
  private val TAGS = "tags"
  private val TAG_WATCHLIST = "tag_watchlist"
  private val TAG_FEEDS = "tag_feeds"
  private val WEBSITES = "websites"

  def isValid(request: HttpServletRequest): Boolean = {
    val tags = request.getAttribute(TAGS).asInstanceOf[Seq[Tag]]
    tags != null && tags.size == 1
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]] = {

    def populateTagPageModelAndView(tag: Tag): Future[Option[ModelMap]] = {
      val eventualTaggedNewsitems = contentRetrievalService.getTaggedNewsitems(tag, MAX_NEWSITEMS, loggedInUser)
      val eventualChildTags = tagDAO.loadTagsByParent(tag._id)
      val eventualMaybeParent = tag.parent.map { pid =>
        tagDAO.loadTagByObjectId(pid)
      }.getOrElse {
        Future.successful(None)
      }

      val eventualMaybeModel = for {
        taggedNewsitemsAndTotalCount <- eventualTaggedNewsitems
        children <- eventualChildTags
        maybeParent <- eventualMaybeParent

      } yield {
        val totalNewsitems = taggedNewsitemsAndTotalCount._2

        val mv = new ModelMap().
          addAttribute(TAG, tag).
          addAttribute("heading", tag.display_name).
          addAttribute("rss_feed_label", tag.display_name.toLowerCase).
          addAttribute("description", rssUrlBuilder.getRssDescriptionForTag(tag)).
          addAttribute("link", urlBuilder.fullyQualified(urlBuilder.getTagUrl(tag))).
          addAttribute("parent", maybeParent.orNull)

        val taggedNewsitems = taggedNewsitemsAndTotalCount._1
        mv.addAttribute(MAIN_CONTENT, taggedNewsitems.asJava)
        mv.addAttribute("main_heading", tag.display_name + " related newsitems")

        setRss(mv)

        if (totalNewsitems > MAX_NEWSITEMS) {
          monthOfLastItem(taggedNewsitems).foreach { i =>
            val link = TagArchiveLink(tag = tag, interval = i, count = None)
            mv.addAttribute("more", urlBuilder.getArchiveLinkUrl(link))
          }
        }

        if (children.nonEmpty) {
          mv.addAttribute("children", children.asJava)
        }

        Some(mv)

      }

      eventualMaybeModel.map { mvo =>
        mvo.map { mv =>
          val actions = if (editPermissionService.canEdit(tag, loggedInUser)) {
            Seq(
              Action("Edit", adminUrlBuilder.getEditTagUrl(tag)),
              Action("Delete", adminUrlBuilder.deleteTagUrl(tag)),
              Action("Autotag", urlBuilder.getAutoTagUrl(tag))
            )
          } else {
            Seq.empty
          }
          mv.addAttribute("actions", actions.asJava)
        }
      }
    }

    populateTagPageModelAndView(tagFromRequest(request))
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[ModelMap] = {
    val tag = tagFromRequest(request)

    val eventualGeotaggedNewsitems = contentRetrievalService.getGeotaggedNewsitemsForTag(tag, MAX_NUMBER_OF_GEOTAGGED_TO_SHOW, loggedInUser = loggedInUser)
    val eventualTaggedWebsites = contentRetrievalService.getTaggedWebsites(tag, MAX_WEBSITES, loggedInUser)
    val eventualRelatedTagLinks = relatedTagsService.getRelatedTagsForTag(tag, 8, loggedInUser)
    val eventualRelatedPublishersForTag = relatedTagsService.getRelatedPublishersForTag(tag, 8, loggedInUser)
    val eventualTagWatchlist = contentRetrievalService.getTagWatchlist(tag, loggedInUser)
    val eventualTagFeeds = contentRetrievalService.getTaggedFeeds(tag, loggedInUser)

    for {
      geotaggedNewsitems <- eventualGeotaggedNewsitems
      taggedWebsites <- eventualTaggedWebsites
      relatedTagLinks <- eventualRelatedTagLinks
      relatedPublishersForTag <- eventualRelatedPublishersForTag
      tagWatchList <- eventualTagWatchlist
      tagFeeds <- eventualTagFeeds
      archiveLinks <- contentRetrievalService.getTagArchiveMonths(tag, loggedInUser)
      latestNewsitems <- latestNewsitems(loggedInUser)

    } yield {
      val mv = new ModelMap()
      mv.addAttribute(WEBSITES, taggedWebsites.asJava)

      if (relatedTagLinks.nonEmpty) {
        mv.addAttribute("related_tags", relatedTagLinks.asJava)
      }
      if (relatedPublishersForTag.nonEmpty) {
        mv.addAttribute("related_publishers", relatedPublishersForTag.asJava)
      }
      if (geotaggedNewsitems.nonEmpty) {
        mv.addAttribute("geocoded", geotaggedNewsitems.asJava)
      }
      if (archiveLinks.nonEmpty) {
        val tagArchiveLinks = archiveLinks.map { a =>
          TagArchiveLink(tag = tag, interval = a.interval, count = a.count)
        }
        mv.addAttribute("archive_links", tagArchiveLinks.asJava)
      }
      mv.addAttribute(TAG_WATCHLIST, tagWatchList.asJava)
      mv.addAttribute(TAG_FEEDS, tagFeeds.asJava)
      mv.addAllAttributes(latestNewsitems)
      mv
    }
  }

  def getViewName(mv: ModelMap, loggedInUser: Option[User]): String = {
    val mainContent = mv.get(MAIN_CONTENT).asInstanceOf[util.List[Resource]]
    val taggedWebsites = mv.get(WEBSITES).asInstanceOf[util.List[Resource]]
    val tagWatchlist = mv.get(TAG_WATCHLIST).asInstanceOf[util.List[Resource]]
    val tagFeeds = mv.get(TAG_FEEDS).asInstanceOf[util.List[Resource]]

    val hasSecondaryContent = !taggedWebsites.isEmpty || !tagWatchlist.isEmpty || !tagFeeds.isEmpty
    val isOneContentType = mainContent.isEmpty || !hasSecondaryContent

    if (isOneContentType) {
      "tagOneContentType"
    } else {
      "tag"
    }
  }

  def setRss(mv: ModelMap): ModelMap = {
    val tag = mv.get(TAG).asInstanceOf[Tag]
    val mainContent = mv.get(MAIN_CONTENT).asInstanceOf[util.List[FrontendResource]].asScala
    if (mainContent.nonEmpty) {
      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForTag(tag), rssUrlBuilder.getRssUrlForTag(tag))
    }
    mv
  }

  private def tagFromRequest(request: HttpServletRequest): Tag = {
    val tags = request.getAttribute(TAGS).asInstanceOf[Seq[Tag]]
    tags.head
  }

}
