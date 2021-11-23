package nz.co.searchwellington.controllers

import nz.co.searchwellington.model.User

import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext

trait EnsuredSubmitter {

  val anonUserService: AnonUserService

  def ensuredSubmittingUser(loggedInUser: Option[User])(implicit ec: ExecutionContext): User = {
    def createAnonUser: User = {
      anonUserService.createAnonUser
    }
    loggedInUser.getOrElse(createAnonUser)
  }

  def submissionShouldBeHeld(owner: Option[User]): Boolean = {
    !owner.exists(_.isAdmin)
  }

  def trimToOption(i: String): Option[String] = { // TODO push to somewhere more sensible
    // Given a non null string check that it's not just blank space
    if (i.trim.nonEmpty) {
      Some(i.trim)
    } else {
      None
    }
  }

  // TODO Spike In a Scala  / functional code base replacing the Spring request scoped loggedInUserFilter with
  // a trait and injecting the request into all calling controllers might be a better fit
  def getLoggedInUser(request: HttpServletRequest): Option[User] = {
    val sessionUser = request.getSession.getAttribute("user").asInstanceOf[User]
    Option(sessionUser)
  }

  def setSignedInUser(request: HttpServletRequest, user: User): Unit = {
    request.getSession.setAttribute("user", user)
  }

}
