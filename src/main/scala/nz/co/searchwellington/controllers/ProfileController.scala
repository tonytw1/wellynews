package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

@Order(3)
@Controller
class ProfileController @Autowired()(mongoRepository: MongoRepository, loggedInUserFilter: LoggedInUserFilter, urlBuilder: UrlBuilder,
                                     val contentRetrievalService: ContentRetrievalService)
  extends ReasonableWaits with CommonModelObjectsService {

  @RequestMapping(Array("/profiles"))
  def profiles: ModelAndView = {
    val loggedInUser = loggedInUserFilter.getLoggedInUser

    Await.result(for {
      users <- mongoRepository.getAllUsers
      mv = {
        new ModelAndView("profiles").
          addObject("heading", "Profiles").
          addObject("profiles", users.asJava)
      }
      w <- withCommonLocal(mv)
      n <- withLatestNewsitems(w, loggedInUser)
    } yield {
      n
    }, TenSeconds)
  }

  @RequestMapping(Array("/profile/edit")) def edit(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val loggedInUser = loggedInUserFilter.getLoggedInUser

    Await.result(withCommonLocal(new ModelAndView("editProfile").
      addObject("heading", "Editing your profile").
      addObject("user", loggedInUser)), TenSeconds)
  }

  // TODO reinstate
  @RequestMapping(value = Array("/profile/edit"), method = Array(RequestMethod.POST)) def save(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    loggedInUserFilter.getLoggedInUser.map { loggedInUser =>
      if (request.getParameter("profilename") != null && Await.result(isValidAvailableProfilename(request.getParameter("profilename")), TenSeconds)) {
        //  loggedInUser.setProfilename(request.getParameter("profilename"))
      }
      //  loggedInUser.setName(request.getParameter("name"))
      // loggedInUser.setBio(request.getParameter("bio"))
      //   loggedInUser.setUrl(request.getParameter("url"))
      mongoRepository.saveUser(loggedInUser)
      new ModelAndView(new RedirectView(urlBuilder.getProfileUrlFromProfileName(loggedInUser.getProfilename)))
    }.getOrElse {
      null //TODO
    }
  }

  def isValidAvailableProfilename(profilename: String): Future[Boolean] = {
    for {
      existingUser <- mongoRepository.getUserByProfilename(profilename)
    } yield {
      profilename.matches("[a-z|A-Z|0-9]+") && existingUser.isEmpty
    }
  }

}