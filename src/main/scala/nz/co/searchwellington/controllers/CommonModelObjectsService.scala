package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.ui.ModelMap

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

trait CommonModelObjectsService extends ReasonableWaits {

  def contentRetrievalService: ContentRetrievalService

  def commonLocal()(implicit ec: ExecutionContext): Future[ModelMap] = {
    val mv = new ModelMap()
    val eventualTopLevelTags = contentRetrievalService.getTopLevelTags
    val eventualFeaturedTags = contentRetrievalService.getFeaturedTags
    for {
      topLevelTags <- eventualTopLevelTags
      featuredTags <- eventualFeaturedTags
    } yield {
      mv.addAttribute("top_level_tags",topLevelTags.asJava)
      mv.addAttribute("featuredTags", featuredTags.asJava)
    }
  }

  def latestNewsitems(loggedInUser: Option[User])(implicit ec: ExecutionContext): Future[ModelMap] = {
    for {
      latestNewsitems <- contentRetrievalService.getLatestNewsitems(5, loggedInUser = loggedInUser)
    } yield {
      new ModelMap().addAttribute("latest_newsitems", latestNewsitems.asJava)
    }
  }

}