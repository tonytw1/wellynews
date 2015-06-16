package nz.co.searchwellington.controllers.models.helpers

import java.util.List
import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.controllers.{RelatedTagsService, RssUrlBuilder}
import nz.co.searchwellington.model.{Tag, TagContentCount, Website}
import nz.co.searchwellington.model.frontend.{FrontendResource, FrontendWebsite}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

@Component class PublisherTagCombinerModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService, rssUrlBuilder: RssUrlBuilder, urlBuilder: UrlBuilder, relatedTagsService: RelatedTagsService, commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder {

  private val logger: Logger = Logger.getLogger(classOf[PublisherTagCombinerModelBuilder])

  def isValid(request: HttpServletRequest): Boolean = {
    val tag: Tag = request.getAttribute("tag").asInstanceOf[Tag]
    val publisher: Website = request.getAttribute("publisher").asInstanceOf[Website]
    return publisher != null && tag != null
  }

  def populateContentModel(request: HttpServletRequest): ModelAndView = {
    if (isValid(request)) {
      logger.info("Building publisher tag combiner page model")
      val tag: Tag = request.getAttribute("tag").asInstanceOf[Tag]
      val publisher: Website = request.getAttribute("publisher").asInstanceOf[Website]
      val mv: ModelAndView = new ModelAndView
      val frontendPublisher: FrontendWebsite = new FrontendWebsite
      frontendPublisher.setName(publisher.getName)
      frontendPublisher.setUrlWords(publisher.getUrlWords)
      mv.addObject("publisher", frontendPublisher)
      mv.addObject("heading", publisher.getName + " and " + tag.getDisplayName)
      mv.addObject("description", "")
      mv.addObject("link", urlBuilder.getPublisherCombinerUrl(publisher.getName, tag))
      populatePublisherTagCombinerNewsitems(mv, publisher, tag)
      return mv
    }
    return null
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    val publisher: Website = request.getAttribute("publisher").asInstanceOf[Website]
    val relatedTagLinks: List[TagContentCount] = relatedTagsService.getRelatedLinksForPublisher(publisher)
    if (relatedTagLinks.size > 0) {
      mv.addObject("related_tags", relatedTagLinks)
    }
  }

  def getViewName(mv: ModelAndView): String = {
    return "publisherCombiner"
  }

  private def populatePublisherTagCombinerNewsitems(mv: ModelAndView, publisher: Website, tag: Tag) {
    val publisherNewsitems: List[FrontendResource] = contentRetrievalService.getPublisherTagCombinerNewsitems(publisher, tag, CommonAttributesModelBuilder.MAX_NEWSITEMS)
    mv.addObject("main_content", publisherNewsitems)
    if (publisherNewsitems.size > 0) {
      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForPublisherCombiner(publisher, tag), rssUrlBuilder.getRssUrlForPublisherCombiner(publisher, tag))
    }
  }
}