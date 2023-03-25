package nz.co.searchwellington.controllers.admin

import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RequiringLoggedInUser, UrlStack}
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.SuppressionDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.Await

@Controller class SuppressionController @Autowired()(suppressionDAO: SuppressionDAO, urlStack: UrlStack,
                                                     val loggedInUserFilter: LoggedInUserFilter) extends RequiringLoggedInUser
  with ReasonableWaits{

  @RequestMapping(Array("/suppress/suppress"))
  def suppress(request: HttpServletRequest): ModelAndView = {
    def suppress(loggedInUser: User): ModelAndView = {
      Option(request.getParameter("url")).foreach { url =>
        Await.result(suppressionDAO.addSuppression(url), TenSeconds)
      }
      returnRedirect(request)
    }

    requiringAdminUser(suppress)
  }

  @RequestMapping(Array("/suppress/unsuppress"))
  def unsuppress(request: HttpServletRequest): ModelAndView = {
    def unsuppress(loggedInUser: User): ModelAndView = {
      if (request.getParameter("url") != null) {
        Await.result(suppressionDAO.removeSupressionForUrl(request.getParameter("url")), TenSeconds)
      }
      returnRedirect(request)
    }

    requiringAdminUser(unsuppress)
  }

  private def returnRedirect(request: HttpServletRequest): ModelAndView = {
    new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)))
  }

}