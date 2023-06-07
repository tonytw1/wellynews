package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

@Order(3)
@Controller
class ProfileController @Autowired()(mongoRepository: MongoRepository, loggedInUserFilter: LoggedInUserFilter,
                                     val contentRetrievalService: ContentRetrievalService)
  extends ReasonableWaits with CommonModelObjects {

  @GetMapping(Array("/profile/edit")) def edit(): ModelAndView = {
    val loggedInUser = loggedInUserFilter.getLoggedInUser
    Await.result(for {
      commonLocal <- commonLocal
    } yield {
      new ModelAndView("editProfile").
        addObject("heading", "Editing your profile").
        addObject("user", loggedInUser).addAllObjects(commonLocal)
    }, TenSeconds)
  }

  /*
  // TODO reinstate
  @PostMapping(Array("/profile/edit")) def save(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
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
  */

  def isValidAvailableProfilename(profilename: String): Future[Boolean] = {
    for {
      existingUser <- mongoRepository.getUserByProfilename(profilename)
    } yield {
      profilename.matches("[a-z|A-Z|0-9]+") && existingUser.isEmpty
    }
  }

}