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

  @RequestMapping(Array("/supress/supress"))
  def supress(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView
    val loggedInUser = loggedInUserFilter.getLoggedInUser
    if (loggedInUser != null && request.getParameter("url") != null) {
      val urlToSupress = request.getParameter("url")
      suppressionDAO.addSuppression(urlToSupress)
    }
    setRedirect(mv, request)
    mv
  }

  @RequestMapping(Array("/supress/unsupress"))
  def unsupress(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView
    val loggedInUser = loggedInUserFilter.getLoggedInUser
    if (loggedInUser != null && request.getParameter("url") != null) {
      suppressionDAO.removeSupressionForUrl(request.getParameter("url"))
    }
    setRedirect(mv, request)
    mv
  }

  private def setRedirect(modelAndView: ModelAndView, request: HttpServletRequest) = modelAndView.setView(new RedirectView(urlStack.getExitUrlFromStack(request)))
}