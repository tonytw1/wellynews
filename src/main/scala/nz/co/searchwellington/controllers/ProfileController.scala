package nz.co.searchwellington.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Order(3)
@Controller
class ProfileController @Autowired()(mongoRepository: MongoRepository, loggedInUserFilter: LoggedInUserFilter, urlBuilder: UrlBuilder,
                                     val contentRetrievalService: ContentRetrievalService)
  extends ReasonableWaits with CommonModelObjectsService {

  private val log = Logger.getLogger(classOf[ProfileController])

  @RequestMapping(Array("/profiles"))
  def profiles(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val eventualUsers = mongoRepository.getAllUsers

    Await.result((for {
      users <- eventualUsers
    } yield {
      import scala.collection.JavaConverters._
      new ModelAndView("profiles").
        addObject("heading", "Profiles").
        addObject("profiles", users.asJava)
    }).flatMap {
      withCommonLocal
    }, TenSeconds)
  }

  @RequestMapping(Array("/profiles/*"))
  def profile(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {

    def userByPath(path: String): Option[User] = {
      if (path.matches("^/profiles/.*$")) {
        val profilename = path.split("/")(2)
        Await.result(mongoRepository.getUserByProfilename(profilename), TenSeconds)
      } else {
        None
      }
    }

    val path = request.getPathInfo
    userByPath(path).map { user =>
      val eventualOwnedBy = contentRetrievalService.getOwnedBy(user, Option(loggedInUserFilter.getLoggedInUser))
      val eventualTaggedBy = contentRetrievalService.getTaggedBy(user, Option(loggedInUserFilter.getLoggedInUser))

      Await.result((for {
        ownedBy <- eventualOwnedBy
        taggedBy <- eventualTaggedBy
      } yield {

        var mv = new ModelAndView("viewProfile")
        val loggedInUser = loggedInUserFilter.getLoggedInUser
        if (loggedInUser != null && loggedInUser.getId == user.getId) {
          mv = new ModelAndView("profile")
        }

        import scala.collection.JavaConverters._
        mv.addObject("heading", "User profile").
          addObject("profileuser", user).
          addObject("submitted", ownedBy.asJava).
          addObject("tagged", taggedBy.asJava)
      }).flatMap(withCommonLocal), TenSeconds)

    }.getOrElse {
      log.info("User not found for path: " + path)
      response.setStatus(HttpServletResponse.SC_NOT_FOUND)
      null
    }
  }

  @RequestMapping(Array("/profile/edit")) def edit(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val loggedInUser = loggedInUserFilter.getLoggedInUser

    Await.result(withCommonLocal(new ModelAndView("editProfile").
      addObject("heading", "Editing your profile").
      addObject("user", loggedInUser)), TenSeconds)
  }

  // TODO reinstate
  @RequestMapping(value = Array("/profile/edit"), method = Array(RequestMethod.POST)) def save(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    Option(loggedInUserFilter.getLoggedInUser).map { loggedInUser =>
      if (request.getParameter("profilename") != null && isValidAvailableProfilename(request.getParameter("profilename"))) {
        //  loggedInUser.setProfilename(request.getParameter("profilename"))
      }
      //  loggedInUser.setName(request.getParameter("name"))
      // loggedInUser.setBio(request.getParameter("bio"))
      //   loggedInUser.setUrl(request.getParameter("url"))
      mongoRepository.saveUser(loggedInUser)
    }

    new ModelAndView(new RedirectView(urlBuilder.getProfileUrlFromProfileName(Option(loggedInUserFilter.getLoggedInUser).get.getProfilename)))
  }

  def isValidAvailableProfilename(profilename: String): Boolean = {
    profilename.matches("[a-z|A-Z|0-9]+") && Await.result(mongoRepository.getUserByProfilename(profilename), TenSeconds).isEmpty
  }

}