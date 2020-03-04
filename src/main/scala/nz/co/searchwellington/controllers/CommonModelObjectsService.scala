package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait CommonModelObjectsService extends ReasonableWaits {

  def contentRetrievalService: ContentRetrievalService

  def withCommonLocal(mv: ModelAndView): Future[ModelAndView] = {
    populateCommonLocal(mv)
  }

  private def populateCommonLocal(mv: ModelAndView): Future[ModelAndView] = {
    val eventualTopLevelTags = contentRetrievalService.getTopLevelTags
    val eventualFeaturedTags = contentRetrievalService.getFeaturedTags
    for {
      topLevelTags <- eventualTopLevelTags
      featuredTags <- eventualFeaturedTags
    } yield {
      import scala.collection.JavaConverters._
      mv.addObject("top_level_tags",topLevelTags.asJava)
      mv.addObject("featuredTags", featuredTags.asJava)
    }
  }

}