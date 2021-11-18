package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

trait CommonModelObjectsService extends ReasonableWaits {

  def contentRetrievalService: ContentRetrievalService

  def withCommonLocal(mv: ModelAndView)(implicit ec: ExecutionContext): Future[ModelAndView] = {
    populateCommonLocal(mv)
  }

  def withLatestNewsitems(mv: ModelAndView, loggedInUser: Option[User])(implicit ec: ExecutionContext): Future[ModelAndView] = {
    for {
      latestNewsitems <- contentRetrievalService.getLatestNewsitems(5, loggedInUser = loggedInUser)
    } yield {
      mv.addObject("latest_newsitems", latestNewsitems.asJava)
    }
  }

  private def populateCommonLocal(mv: ModelAndView)(implicit ec: ExecutionContext): Future[ModelAndView] = {
    val eventualTopLevelTags = contentRetrievalService.getTopLevelTags
    val eventualFeaturedTags = contentRetrievalService.getFeaturedTags
    for {
      topLevelTags <- eventualTopLevelTags
      featuredTags <- eventualFeaturedTags
    } yield {
      mv.addObject("top_level_tags",topLevelTags.asJava)
      mv.addObject("featuredTags", featuredTags.asJava)
    }
  }

}