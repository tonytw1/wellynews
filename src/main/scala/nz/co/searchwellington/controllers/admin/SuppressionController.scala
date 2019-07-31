package nz.co.searchwellington.controllers.admin

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.controllers.{LoggedInUserFilter, UrlStack}
import nz.co.searchwellington.repositories.SuppressionDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

@Controller class SuppressionController @Autowired()(suppressionDAO: SuppressionDAO, urlStack: UrlStack,
                                                     loggedInUserFilter: LoggedInUserFilter) {
  @RequestMapping(Array("/suppress/suppress"))
  def suppress(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val loggedInUser = loggedInUserFilter.getLoggedInUser

    Option(loggedInUser).map { user =>
      Option(request.getParameter("url")).map { url =>
        suppressionDAO.addSuppression(url)
      }
    }

    returnRedirect(request)
  }

  @RequestMapping(Array("/suppress/unsuppress"))
  def unsuppress(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView
    val loggedInUser = loggedInUserFilter.getLoggedInUser
    if (loggedInUser != null && request.getParameter("url") != null) {
      suppressionDAO.removeSupressionForUrl(request.getParameter("url"))
    }
    returnRedirect(request)
  }

  private def returnRedirect(request: HttpServletRequest): ModelAndView = {
    new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)))
  }
}