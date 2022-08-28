package nz.co.searchwellington.controllers.admin

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RequiringLoggedInUser}
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.geo.LatLong
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller class AdminIndexController @Autowired()(val loggedInUserFilter: LoggedInUserFilter, mongoRepository: MongoRepository)
  extends RequiringLoggedInUser with ReasonableWaits {

  private val log = LogFactory.getLog(classOf[AdminIndexController])

  @GetMapping(Array("/admin"))
  def acceptAllFrom(request: HttpServletRequest): ModelAndView = {
    def show(loggedInUser: User): ModelAndView = {
      new ModelAndView("adminindex").addObject("heading", "Admin index")
    }
    requiringAdminUser(show)
  }

}
