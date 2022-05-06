package nz.co.searchwellington.controllers

import nz.co.searchwellington.model.User

trait HeldSubmissions {

  def submissionShouldBeHeld(loggerInUser: User): Boolean = {
    !loggerInUser.isAdmin
  }

}
