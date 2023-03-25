package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap

import java.util.regex.Pattern
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Component class ProfileModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                  mongoRepository: MongoRepository) extends ModelBuilder
  with CommonSizes with ReasonableWaits with Pagination {

  private val log = LogFactory.getLog(classOf[ProfileModelBuilder])

  private val profilePageRegex = "^/profiles/(.*?)(/(rss|json))?$"

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches(profilePageRegex)
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]] = {
    val path = RequestPath.getPathFrom(request)

    def userByPath(path: String): Future[Option[User]] = {
      val pattern = Pattern.compile(profilePageRegex)
      val matcher = pattern.matcher(path)

      if (matcher.matches) {
        matcher.group(1)
        val profilename = path.split("/")(2)
        log.info(s"Fetching user by profile name {profilename}")
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
          latestNewsitems <- latestNewsitems(loggedInUser)
        } yield {
          val mv = new ModelMap().
            addAttribute("heading", "User profile").
            addAttribute("profileuser", user).
            addAttribute(MAIN_CONTENT, submissions._1.asJava).
            addAttribute("tagged", tagged.asJava).
            addAllAttributes(latestNewsitems)
          Some(mv)
        }

      }.getOrElse {
        Future.successful(None)
      }
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[ModelMap] = {
    Future.successful(new ModelMap())
  }

  def getViewName(mv: ModelMap, loggedInUser: Option[User]): String = {
    loggedInUser.flatMap { user =>
      Option(mv.get("profileuser").asInstanceOf[User]).map { profileUser =>
        if (user == profileUser) {
          "profile"
        } else {
          "viewProfile"
        }
      }
    }.getOrElse("viewProfile")
  }

}
