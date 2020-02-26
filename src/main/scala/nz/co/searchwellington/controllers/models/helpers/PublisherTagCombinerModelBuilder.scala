package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Tag, User, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class PublisherTagCombinerModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService, rssUrlBuilder: RssUrlBuilder, urlBuilder: UrlBuilder,
                                                               relatedTagsService: RelatedTagsService, commonAttributesModelBuilder: CommonAttributesModelBuilder,
                                                               frontendResourceMapper: FrontendResourceMapper) extends ModelBuilder
  with CommonSizes with ReasonableWaits {

  private val logger = Logger.getLogger(classOf[PublisherTagCombinerModelBuilder])

  def isValid(request: HttpServletRequest): Boolean = {
    val tag = request.getAttribute("tag").asInstanceOf[Tag]
    val publisher = request.getAttribute("publisher").asInstanceOf[Website]
    publisher != null && tag != null
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: User): Future[Option[ModelAndView]] = {
    if (isValid(request)) {
      logger.info("Building publisher tag combiner page model")
      val tag = request.getAttribute("tag").asInstanceOf[Tag]
      val publisher = request.getAttribute("publisher").asInstanceOf[Website]

      for {
        publisherTagNewsitems <- contentRetrievalService.getPublisherTagCombinerNewsitems(publisher, tag, MAX_NEWSITEMS, Option(loggedInUser))

      } yield {
        import scala.collection.JavaConverters._
        val mv = new ModelAndView().
          addObject("publisher", frontendResourceMapper.mapFrontendWebsite(publisher)).
          addObject("heading", publisher.title.getOrElse("") + " and " + tag.getDisplayName).
          addObject("description", "").
          addObject("link", urlBuilder.getPublisherCombinerUrl(publisher, tag)).
          addObject(MAIN_CONTENT, publisherTagNewsitems.asJava)

        if (publisherTagNewsitems.nonEmpty) {
          commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForPublisherCombiner(publisher, tag), rssUrlBuilder.getRssUrlForPublisherCombiner(publisher, tag))
        }

        Some(mv)
      }

    } else {
      Future.successful(None)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: User) {
    val publisher = request.getAttribute("publisher").asInstanceOf[Website]
    val relatedTagLinks = relatedTagsService.getRelatedLinksForPublisher(publisher)
    if (relatedTagLinks.nonEmpty) {
      mv.addObject("related_tags", relatedTagLinks)
    }
  }

  def getViewName(mv: ModelAndView): String = "publisherCombiner"

}
