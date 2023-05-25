package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.attributesetters.PublisherPageAttributeSetter
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Tag, User, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.tagging.RelatedTagsService
import nz.co.searchwellington.urls.{RssUrlBuilder, UrlBuilder}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Component class PublisherTagCombinerModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService, rssUrlBuilder: RssUrlBuilder, urlBuilder: UrlBuilder,
                                                               relatedTagsService: RelatedTagsService, commonAttributesModelBuilder: CommonAttributesModelBuilder,
                                                               frontendResourceMapper: FrontendResourceMapper) extends ModelBuilder
  with CommonSizes with ReasonableWaits with Pagination {

  def isValid(request: HttpServletRequest): Boolean = {
    val tag = request.getAttribute("tag").asInstanceOf[Tag]
    val publisher = request.getAttribute(PublisherPageAttributeSetter.PUBLISHER_ATTRIBUTE).asInstanceOf[Website]
    publisher != null && tag != null
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]] = {
    val tag = request.getAttribute("tag").asInstanceOf[Tag]
    val publisher = request.getAttribute(PublisherPageAttributeSetter.PUBLISHER_ATTRIBUTE).asInstanceOf[Website]
    val eventualFrontendPublisher = frontendResourceMapper.createFrontendResourceFrom(publisher)
    for {
      frontendPublisher <- eventualFrontendPublisher
      publisherTagNewsitems <- contentRetrievalService.getPublisherTagCombinerNewsitems(publisher, tag, 0, MAX_NEWSITEMS, loggedInUser) // TODO pagination
    } yield {
      val mv = new ModelMap().
        addAttribute("publisher", frontendPublisher).
        addAttribute("tag", tag).
        addAttribute("heading", publisher.title + " and " + tag.getDisplayName).
        addAttribute("description", "Items tagged with " + publisher.getTitle + " and " + tag.getDisplayName + ".").
        addAttribute("link", urlBuilder.fullyQualified(urlBuilder.getPublisherCombinerUrl(publisher, tag))).
        addAttribute("rss_url", rssUrlBuilder.getRssUrlForPublisherTagCombiner(publisher, tag)).
        addAttribute(MAIN_CONTENT, publisherTagNewsitems.asJava)

      if (publisherTagNewsitems.nonEmpty) {
        commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForPublisherCombiner(publisher, tag), rssUrlBuilder.getRssUrlForPublisherTagCombiner(publisher, tag))
      }

      Some(mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[ModelMap] = {
    val publisher = request.getAttribute(PublisherPageAttributeSetter.PUBLISHER_ATTRIBUTE).asInstanceOf[Website]
    for {
      relatedTags <- relatedTagsService.getRelatedTagsForPublisher(publisher, loggedInUser)
      latestNewsitems <- latestNewsitems(loggedInUser)
    } yield {
      val mv = new ModelMap().addAllAttributes(latestNewsitems)
      if (relatedTags.nonEmpty) {
        mv.addAttribute("related_tags", relatedTags.asJava)
      }
      mv
    }
  }

  def getViewName(mv: ModelMap, loggedInUser: Option[User]): String = "publisherTagCombiner"

}
