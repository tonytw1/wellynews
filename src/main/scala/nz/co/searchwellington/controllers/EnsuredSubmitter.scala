package nz.co.searchwellington.controllers

import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.model.User

import scala.concurrent.ExecutionContext

trait EnsuredSubmitter {

  val anonUserService: AnonUserService

  def ensuredSubmittingUser(loggedInUser: Option[User])(implicit ec: ExecutionContext): User = {
    def createAnonUser: User = {
      anonUserService.createAnonUser
    }
    loggedInUser.getOrElse(createAnonUser)
  }

  def submissionShouldBeHeld(owner: Option[User]): Boolean = {  // TODO weird placement
    !owner.exists(_.isAdmin)
  }

  def setSignedInUser(request: HttpServletRequest, user: User): Unit = {
    request.getSession.setAttribute("user", user)
  }

}
