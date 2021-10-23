package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class ProfileModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                  mongoRepository: MongoRepository) extends ModelBuilder
  with CommonSizes with ReasonableWaits with Pagination {

  private val logger = Logger.getLogger(classOf[ProfileModelBuilder])

  private val profilePageRegex = "^/profiles/(.*?)(/(rss|json))?$"

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches(profilePageRegex)
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {
    val path = RequestPath.getPathFrom(request)

    def userByPath(path: String): Future[Option[User]] = {
      val pattern = Pattern.compile(profilePageRegex)
      val matcher = pattern.matcher(path)

      if (matcher.matches) {
        matcher.group(1)
        val profilename = path.split("/")(2)
        logger.info(s"Fetching user by profile name {profilename}")
        mongoRepository.getUserByProfilename(profilename)

      } else {
        Future.successful(None)
      }
    }

    userByPath(path).flatMap { maybeUser =>
      maybeUser.map{ user =>
        for {
          submissions <- contentRetrievalService.getOwnedBy(user, loggedInUser, MAX_NEWSITEMS)
          tagged <- contentRetrievalService.getTaggedBy(user, loggedInUser)
          mv = {
            import scala.collection.JavaConverters._
            new ModelAndView().
              addObject("heading", "User profile").
              addObject("profileuser", user).
              addObject(MAIN_CONTENT, submissions._1.asJava).
              addObject("tagged", tagged.asJava)
          }
          withNewsitems <- withLatestNewsitems(mv, loggedInUser)

        } yield {
          Some(withNewsitems)
        }

      }.getOrElse {
        Future.successful(None)
      }
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = {
    Future.successful(mv)
  }

  def getViewName(mv: ModelAndView) = "viewProfile"

  /*
  def selectView: String = {
    loggedInUser.map { u =>
      if (u.getId == user.getId) {
        "profile"
      } else {
        "viewProfile"
      }
    }.getOrElse {
      "viewProfile"
    }
  }
  */

}
