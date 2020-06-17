package nz.co.searchwellington.controllers.admin

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RequiringLoggedInUser}
import nz.co.searchwellington.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller class AdminIndexController @Autowired()(val loggedInUserFilter: LoggedInUserFilter) extends RequiringLoggedInUser {

  @RequestMapping(Array("/admin"))
  def acceptAllFrom(request: HttpServletRequest): ModelAndView = {
    def show(loggedInUser: User): ModelAndView = {
      new ModelAndView("adminindex").addObject("heading", "Admin index")
    }
    requiringAdminUser(show)
  }

}
