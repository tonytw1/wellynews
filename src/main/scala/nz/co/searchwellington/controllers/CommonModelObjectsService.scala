package nz.co.searchwellington.controllers

import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

@Component class CommonModelObjectsService @Autowired() (contentRetrievalService: ContentRetrievalService) {

  def populateCommonLocal(mv: ModelAndView) {
    import scala.collection.JavaConverters._
    mv.addObject("top_level_tags", contentRetrievalService.getTopLevelTags.asJava)
    mv.addObject("featuredTags", contentRetrievalService.getFeaturedTags.asJava)
  }

}