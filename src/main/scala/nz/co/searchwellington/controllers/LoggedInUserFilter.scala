package nz.co.searchwellington.controllers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.model.User
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Scope, ScopedProxyMode}
import org.springframework.stereotype.Component

@Component("loggedInUserFilter")
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
class LoggedInUserFilter @Autowired()(request: HttpServletRequest) {

  private val log = LogFactory.getLog(classOf[LoggedInUserFilter])

  def getLoggedInUser: Option[User] = getUserFromSession(request)

  def setLoggedInUser(request: HttpServletRequest, user: User): Unit = {
    log.info("Setting signed in user: " + user)
    request.getSession.setAttribute("user", user)
  }

  def clearLoggedInUser(request: HttpServletRequest): Unit = {
    request.getSession.setAttribute("user", null)
  }

  def isSignedIn: Boolean = getLoggedInUser.nonEmpty

  def getLoggedinUserProfileName: String = {
    getLoggedInUser.flatMap(_.profilename).orNull
  }

  def getLoggedInUserOrNull: User = {
    getLoggedInUser.orNull
  }

  private def getUserFromSession(request: HttpServletRequest): Option[User] = {
    val sessionUser = request.getSession.getAttribute("user").asInstanceOf[User]
    Option(sessionUser)
  }

}
