package nz.co.searchwellington.controllers

import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

@Component class CommonModelObjectsService @Autowired()(contentRetrievalService: ContentRetrievalService,
                                                        frontendResourceMapper: FrontendResourceMapper) {
  def populateCommonLocal(mv: ModelAndView) {
    import scala.collection.JavaConverters._

    val topLevelTags = contentRetrievalService.getTopLevelTags
    mv.addObject("top_level_tags", topLevelTags.asJava)

    val featuredTags = contentRetrievalService.getFeaturedTags
    mv.addObject("featuredTags", featuredTags.asJava)
  }

}