package nz.co.searchwellington.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.Await

@Controller
class ProfileController @Autowired()(mongoRepository: MongoRepository, loggedInUserFilter: LoggedInUserFilter, urlBuilder: UrlBuilder,
                                                 contentRetrievalService: ContentRetrievalService,
                                     commonModelObjectsService: CommonModelObjectsService)
  extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[ProfileController])

  @RequestMapping(Array("/profiles"))
  def all(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv: ModelAndView = new ModelAndView("profiles")
    mv.addObject("heading", "Profiles")
    commonModelObjectsService.populateCommonLocal(mv)
    mv.addObject("profiles", Await.result(mongoRepository.getAllUsers, TenSeconds))
    return mv
  }

  @RequestMapping(Array("/profiles/*"))
  def view(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {

    def userByPath(path: String): Option[User] = {
      if (path.matches("^/profiles/.*$")) {
        val profilename = path.split("/")(2)
        Await.result(mongoRepository.getUserByProfilename(profilename), TenSeconds)
      } else {
        None
      }
    }

    userByPath(request.getPathInfo).map { user =>
      var mv = new ModelAndView("viewProfile")
      val loggedInUser = loggedInUserFilter.getLoggedInUser
      if (loggedInUser != null && loggedInUser.getId == user.getId) {
        mv = new ModelAndView("profile")
      }
      mv.addObject("heading", "User profile")
      commonModelObjectsService.populateCommonLocal(mv)
      mv.addObject("profileuser", user)

      import scala.collection.JavaConverters._
      mv.addObject("submitted", contentRetrievalService.getOwnedBy(user).asJava)
      mv.addObject("tagged", contentRetrievalService.getTaggedBy(user).asJava)
      mv

    }.getOrElse {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND)
      null
    }
  }

  @RequestMapping(Array("/profile/edit")) def edit(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView("editProfile")
    commonModelObjectsService.populateCommonLocal(mv)
    mv.addObject("heading", "Editing your profile")
    val loggedInUser = loggedInUserFilter.getLoggedInUser
    mv.addObject("user", loggedInUser)
    return mv
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

    return new ModelAndView(new RedirectView(urlBuilder.getProfileUrlFromProfileName(Option(loggedInUserFilter.getLoggedInUser).get.getProfilename)))
  }

  def isValidAvailableProfilename(profilename: String): Boolean = {
    profilename.matches("[a-z|A-Z|0-9]+") && Await.result(mongoRepository.getUserByProfilename(profilename), TenSeconds).isEmpty
  }

}