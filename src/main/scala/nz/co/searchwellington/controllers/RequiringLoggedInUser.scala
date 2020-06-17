package nz.co.searchwellington.controllers

import nz.co.searchwellington.model.User
import org.apache.log4j.Logger
import org.springframework.web.servlet.ModelAndView

trait RequiringLoggedInUser {

  private val log = Logger.getLogger(classOf[RequiringLoggedInUser])

  def loggedInUserFilter: LoggedInUserFilter

  def requiringAdminUser(action: User => ModelAndView): ModelAndView = {
    loggedInUserFilter.getLoggedInUser.fold {
      log.warn("No logged in user found")
      val notLoggedIn: ModelAndView = null
      notLoggedIn

    } { loggedInUser =>
      if (loggedInUser.isAdmin) {
        action(loggedInUser)
      } else {
        log.warn("User is not an admin")
        val notAnAdmin: ModelAndView = null
        notAnAdmin
      }
    }
  }

}
