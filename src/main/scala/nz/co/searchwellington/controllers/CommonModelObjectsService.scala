package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.Await

trait CommonModelObjectsService extends ReasonableWaits {

  def contentRetrievalService: ContentRetrievalService

  def withCommonLocal(mv: ModelAndView): ModelAndView = {
    populateCommonLocal(mv)
    mv
  }

  private def populateCommonLocal(mv: ModelAndView) {
    import scala.collection.JavaConverters._

    val topLevelTags = contentRetrievalService.getTopLevelTags
    mv.addObject("top_level_tags", Await.result(topLevelTags, TenSeconds).asJava)

    val featuredTags = contentRetrievalService.getFeaturedTags
    mv.addObject("featuredTags", Await.result(featuredTags, TenSeconds).asJava)
  }


}