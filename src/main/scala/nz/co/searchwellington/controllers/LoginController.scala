package nz.co.searchwellington.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

@Controller class LoginController @Autowired()(urlStack: UrlStack,
                                               val contentRetrievalService: ContentRetrievalService,
                                               loggedInUserFilter: LoggedInUserFilter) extends CommonModelObjectsService {

  @RequestMapping(Array("/signin")) def signin(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val loggedInUser = loggedInUserFilter.getLoggedInUser

    Await.result(withCommonLocal {
      new ModelAndView("signin").addObject("heading", "Sign in")
    }.flatMap { mv =>
      withLatestNewsitems(mv, loggedInUser)
    }, TenSeconds)
  }

  @RequestMapping(Array("/logout")) def logout(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {

    def setRedirect(mv: ModelAndView, request: HttpServletRequest) {
      mv.setView(new RedirectView(urlStack.getExitUrlFromStack(request)))
    }

    val mv = new ModelAndView
    request.getSession.setAttribute("user", null)
    setRedirect(mv, request)
    mv
  }

}