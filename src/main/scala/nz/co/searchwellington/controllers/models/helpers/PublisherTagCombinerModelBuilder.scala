package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.controllers.{RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.model.frontend.FrontendWebsite
import nz.co.searchwellington.model.{Tag, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

@Component class PublisherTagCombinerModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService, rssUrlBuilder: RssUrlBuilder, urlBuilder: UrlBuilder, relatedTagsService: RelatedTagsService, commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder with CommonSizes {

  private val logger: Logger = Logger.getLogger(classOf[PublisherTagCombinerModelBuilder])

  def isValid(request: HttpServletRequest): Boolean = {
    val tag = request.getAttribute("tag").asInstanceOf[Tag]
    val publisher = request.getAttribute("publisher").asInstanceOf[Website]
    publisher != null && tag != null
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {

    def populatePublisherTagCombinerNewsitems(mv: ModelAndView, publisher: Website, tag: Tag) {
      val publisherNewsitems = contentRetrievalService.getPublisherTagCombinerNewsitems(publisher, tag, MAX_NEWSITEMS)
      mv.addObject(MAIN_CONTENT, publisherNewsitems)
      if (publisherNewsitems.size > 0) {
        commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForPublisherCombiner(publisher, tag), rssUrlBuilder.getRssUrlForPublisherCombiner(publisher, tag))
      }
    }

    if (isValid(request)) {
      logger.info("Building publisher tag combiner page model")
      val tag = request.getAttribute("tag").asInstanceOf[Tag]
      val publisher = request.getAttribute("publisher").asInstanceOf[Website]

      val mv = new ModelAndView

      val frontendPublisher = new FrontendWebsite // TODO push to mapper
      frontendPublisher.setName(publisher.title.getOrElse(""))
      frontendPublisher.setUrlWords(publisher.url_words.getOrElse(""))

      mv.addObject("publisher", frontendPublisher)
      mv.addObject("heading", publisher.title.getOrElse("") + " and " + tag.getDisplayName)
      mv.addObject("description", "")
      mv.addObject("link", urlBuilder.getPublisherCombinerUrl(publisher.title.getOrElse(""), tag))
      populatePublisherTagCombinerNewsitems(mv, publisher, tag)
      Some(mv)

    } else {
      None
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    val publisher = request.getAttribute("publisher").asInstanceOf[Website]
    val relatedTagLinks = relatedTagsService.getRelatedLinksForPublisher(publisher)
    if (relatedTagLinks.size > 0) {
      mv.addObject("related_tags", relatedTagLinks)
    }
  }

  def getViewName(mv: ModelAndView): String = {
    "publisherCombiner"
  }

}
