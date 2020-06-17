package nz.co.searchwellington.controllers.admin

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RequiringLoggedInUser}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller class AdminIndexController @Autowired()(val loggedInUserFilter: LoggedInUserFilter) extends RequiringLoggedInUser {

  private val log = Logger.getLogger(classOf[AdminIndexController])

  @RequestMapping(Array("/admin"))
  def acceptAllFrom(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    def show(): ModelAndView = {
      new ModelAndView("adminindex").addObject("heading", "Admin index")
    }
    requiringAdminUser(show)
  }

}
