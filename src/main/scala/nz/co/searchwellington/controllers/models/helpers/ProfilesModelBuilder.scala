package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

@Component class ProfilesModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                   mongoRepository: MongoRepository) extends ModelBuilder
  with CommonSizes with ReasonableWaits with Pagination with ContentFields {

  private val profilesPageRegex = "^/profiles$"

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches(profilesPageRegex)
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {
    for {
      users <- mongoRepository.getAllUsers
    } yield {
      Some(new ModelAndView().
        addObject("heading", "Profiles").
        addObject(MAIN_CONTENT, users.asJava))
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User]): Future[ModelMap] = {
    Future.successful(new ModelMap())
  }

  def getViewName(mv: ModelAndView, loggedInUser: Option[User]): String = "profiles"

}
