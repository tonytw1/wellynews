package nz.co.searchwellington.controllers.profiles

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import nz.co.searchwellington.annotations.Timed
import nz.co.searchwellington.controllers.CommonModelObjectsService
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.HibernateBackedUserDAO
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

@Controller class ProfileController @Autowired()(userDAO: HibernateBackedUserDAO, loggerInUserFilter: LoggedInUserFilter, urlBuilder: UrlBuilder, contentRetrievalService: ContentRetrievalService, commonModelObjectsService: CommonModelObjectsService) {

  private val log = Logger.getLogger(classOf[ProfileController])

  @RequestMapping(Array("/profiles"))
  @Timed(timingNotes = "") def all(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv: ModelAndView = new ModelAndView("profiles")
    mv.addObject("heading", "Profiles")
    commonModelObjectsService.populateCommonLocal(mv)
    mv.addObject("profiles", userDAO.getActiveUsers)
    return mv
  }

  @RequestMapping(Array("/profile/edit")) def edit(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv: ModelAndView = new ModelAndView("editProfile")
    commonModelObjectsService.populateCommonLocal(mv)
    mv.addObject("heading", "Editing your profile")
    val loggedInUser: User = loggerInUserFilter.getLoggedInUser
    mv.addObject("user", loggedInUser)
    return mv
  }

  @RequestMapping(value = Array("/profile/edit"), method = Array(RequestMethod.POST)) def save(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mayByloggedInUser = Option(loggerInUserFilter.getLoggedInUser)
    mayByloggedInUser.map { loggedInUser =>
      if (request.getParameter("profilename") != null && isValidNewProfilename(request.getParameter("profilename"))) {
      //  loggedInUser.setProfilename(request.getParameter("profilename"))
      }
    //  loggedInUser.setName(request.getParameter("name"))
     // loggedInUser.setBio(request.getParameter("bio"))
   //   loggedInUser.setUrl(request.getParameter("url"))
      userDAO.saveUser(loggedInUser)
    }

    return new ModelAndView(new RedirectView(urlBuilder.getProfileUrlFromProfileName(mayByloggedInUser.get.getProfilename)))
  }

  @RequestMapping(Array("/profiles/*"))
  @Timed(timingNotes = "") def view(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {

    def userByPath(path: String): Option[User] = {
      if (path.matches("^/profiles/.*$")) {
        val profilename = path.split("/")(2)
        userDAO.getUserByProfileName(profilename)
      } else {
        None
      }
    }

    userByPath(request.getPathInfo).map { user =>
        var mv = new ModelAndView("viewProfile")
        val loggedInUser: User = loggerInUserFilter.getLoggedInUser
        if (loggedInUser != null && loggedInUser.getId == user.getId) {
          mv = new ModelAndView("profile")
        }
        mv.addObject("heading", "User profile")
        commonModelObjectsService.populateCommonLocal(mv)
        mv.addObject("profileuser", user)
        mv.addObject("submitted", contentRetrievalService.getOwnedBy(user))
        mv.addObject("tagged", contentRetrievalService.getTaggedBy(user))
        return mv

    }.getOrElse {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND)
      return null
    }
  }

  def isValidNewProfilename(profilename: String): Boolean = {
    profilename.matches("[a-z|A-Z|0-9]+") && userDAO.getUserByProfileName(profilename) == null
  }

}