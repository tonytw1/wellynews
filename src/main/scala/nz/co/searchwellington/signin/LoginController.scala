package nz.co.searchwellington.signin

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.controllers.{CommonModelObjectsService, LoggedInUserFilter, UrlStack}
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller class LoginController @Autowired()(urlStack: UrlStack,
                                               val contentRetrievalService: ContentRetrievalService,
                                               loggedInUserFilter: LoggedInUserFilter) extends CommonModelObjectsService {

  @RequestMapping(Array("/signin")) def signin(request: HttpServletRequest): ModelAndView = {
    implicit val currentSpan: Span = Span.current()
    val loggedInUser = loggedInUserFilter.getLoggedInUser
    if (loggedInUser.isEmpty) {
      Await.result(for {
        commonLocal <- commonLocal
        latestNewsitems <- latestNewsitems(loggedInUser)
      } yield {
        new ModelAndView("signin").addObject("heading", "Sign in").
          addAllObjects(commonLocal).
          addAllObjects(latestNewsitems)
      }, TenSeconds)
    } else {
      redirectToUrlStack(request)
    }
  }

  @RequestMapping(Array("/logout")) def logout(request: HttpServletRequest): ModelAndView = {
    loggedInUserFilter.clearLoggedInUser(request)
    redirectToUrlStack(request)
  }

  private def redirectToUrlStack(request: HttpServletRequest): ModelAndView = {
    new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)))
  }

}
