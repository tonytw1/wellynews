package nz.co.searchwellington.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

@Controller class LoginController @Autowired()(urlStack: UrlStack, commonModelObjectsService: CommonModelObjectsService) {

  @RequestMapping(Array("/signin")) def signin(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView
    commonModelObjectsService.populateCommonLocal(mv)
    mv.addObject("heading", "Sign in")
    mv.setViewName("signin")
    mv
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