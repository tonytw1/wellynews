package nz.co.searchwellington.controllers

import nz.co.searchwellington.model.User
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component("loggedInUserFilter")
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
class LoggedInUserFilter @Autowired()() {

  private val log = Logger.getLogger(classOf[LoggedInUserFilter])

  private var loggedInUser: User = null

  def loadLoggedInUser(request: HttpServletRequest): Unit = {
    if (request.getSession.getAttribute("user") != null) {
      val sessionUser = request.getSession.getAttribute("user").asInstanceOf[User]
      log.debug("Found user on session: " + sessionUser.getName)
      loggedInUser = sessionUser
    }
    else {
      loggedInUser = null
    }
  }

  def getLoggedInUser: Option[User] = {
    Option(loggedInUser)
  }

  def setLoggedInUser(request: HttpServletRequest, user: User): Unit = {
    log.info("Setting signed in user: " + user)
    request.getSession.setAttribute("user", user)
  }
}