package nz.co.searchwellington.controllers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.model.User
import org.apache.commons.logging.LogFactory
import org.springframework.context.annotation.{Scope, ScopedProxyMode}
import org.springframework.stereotype.Component

@Component("loggedInUserFilter")
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
class LoggedInUserFilter {

  private val log = LogFactory.getLog(classOf[LoggedInUserFilter])

  private var loggedInUser: Option[User] = None

  def loadLoggedInUser(request: HttpServletRequest): Unit = {
    val sessionUser = request.getSession.getAttribute("user").asInstanceOf[User]
    loggedInUser = Option(sessionUser)
  }

  def getLoggedInUser: Option[User] = loggedInUser

  def setLoggedInUser(request: HttpServletRequest, user: User): Unit = {
    log.info("Setting signed in user: " + user)
    request.getSession.setAttribute("user", user)
  }

  def clearLoggedInUser(request: HttpServletRequest): Unit = {
    request.getSession.setAttribute("user", null)
  }

  def isSignedIn: Boolean = loggedInUser.nonEmpty

  def getLoggedinUserProfileName: String = {
    loggedInUser.flatMap(_.profilename).orNull
  }

  def getLoggedInUserOrNull: User = {
    loggedInUser.orNull
  }

}
