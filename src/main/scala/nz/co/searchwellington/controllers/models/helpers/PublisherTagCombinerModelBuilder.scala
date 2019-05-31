package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.controllers.{RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Tag, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

@Component class PublisherTagCombinerModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService, rssUrlBuilder: RssUrlBuilder, urlBuilder: UrlBuilder,
                                                               relatedTagsService: RelatedTagsService, commonAttributesModelBuilder: CommonAttributesModelBuilder,
                                                               frontendResourceMapper: FrontendResourceMapper) extends ModelBuilder with CommonSizes {

  private val logger = Logger.getLogger(classOf[PublisherTagCombinerModelBuilder])

  def isValid(request: HttpServletRequest): Boolean = {
    val tag = request.getAttribute("tag").asInstanceOf[Tag]
    val publisher = request.getAttribute("publisher").asInstanceOf[Website]
    publisher != null && tag != null
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {

    def populatePublisherTagCombinerNewsitems(mv: ModelAndView, publisher: Website, tag: Tag) {
      val publisherNewsitems = contentRetrievalService.getPublisherTagCombinerNewsitems(publisher, tag, MAX_NEWSITEMS)

      import scala.collection.JavaConverters._
      mv.addObject(MAIN_CONTENT, publisherNewsitems.asJava)

      if (publisherNewsitems.nonEmpty) {
        commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForPublisherCombiner(publisher, tag), rssUrlBuilder.getRssUrlForPublisherCombiner(publisher, tag))
      }
    }

    if (isValid(request)) {
      logger.info("Building publisher tag combiner page model")
      val tag = request.getAttribute("tag").asInstanceOf[Tag]
      val publisher = request.getAttribute("publisher").asInstanceOf[Website]

      val mv = new ModelAndView
      mv.addObject("publisher", frontendResourceMapper.mapFrontendWebsite(publisher))
      mv.addObject("heading", publisher.title.getOrElse("") + " and " + tag.getDisplayName)
      mv.addObject("description", "")
      mv.addObject("link", urlBuilder.getPublisherCombinerUrl(publisher, tag))
      populatePublisherTagCombinerNewsitems(mv, publisher, tag)
      Some(mv)

    } else {
      None
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    val publisher = request.getAttribute("publisher").asInstanceOf[Website]
    val relatedTagLinks = relatedTagsService.getRelatedLinksForPublisher(publisher)
    if (relatedTagLinks.nonEmpty) {
      mv.addObject("related_tags", relatedTagLinks)
    }
  }

  def getViewName(mv: ModelAndView): String = "publisherCombiner"

}
