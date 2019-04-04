package nz.co.searchwellington.controllers.profiles

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.annotations.Timed
import nz.co.searchwellington.controllers.{CommonModelObjectsService, LoggedInUserFilter}
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

@Controller class ProfileController @Autowired()(mongoRepository: MongoRepository, loggerInUserFilter: LoggedInUserFilter, urlBuilder: UrlBuilder,
                                                 contentRetrievalService: ContentRetrievalService, commonModelObjectsService: CommonModelObjectsService) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[ProfileController])

  @RequestMapping(Array("/profile/edit")) def edit(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView("editProfile")
    commonModelObjectsService.populateCommonLocal(mv)
    mv.addObject("heading", "Editing your profile")
    val loggedInUser: User = loggerInUserFilter.getLoggedInUser
    mv.addObject("user", loggedInUser)
    return mv
  }

  // TODO reinstate
  @RequestMapping(value = Array("/profile/edit"), method = Array(RequestMethod.POST)) def save(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    Option(loggerInUserFilter.getLoggedInUser).map { loggedInUser =>
      if (request.getParameter("profilename") != null && isValidAvailableProfilename(request.getParameter("profilename"))) {
        //  loggedInUser.setProfilename(request.getParameter("profilename"))
      }
      //  loggedInUser.setName(request.getParameter("name"))
      // loggedInUser.setBio(request.getParameter("bio"))
      //   loggedInUser.setUrl(request.getParameter("url"))
      mongoRepository.saveUser(loggedInUser)
    }

    return new ModelAndView(new RedirectView(urlBuilder.getProfileUrlFromProfileName(Option(loggerInUserFilter.getLoggedInUser).get.getProfilename)))
  }

  def isValidAvailableProfilename(profilename: String): Boolean = {
    profilename.matches("[a-z|A-Z|0-9]+") && Await.result(mongoRepository.getUserByProfilename(profilename), TenSeconds).isEmpty
  }

}