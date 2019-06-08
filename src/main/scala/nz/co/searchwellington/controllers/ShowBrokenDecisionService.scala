package nz.co.searchwellington.controllers

import nz.co.searchwellington.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class ShowBrokenDecisionService @Autowired()() {

  def shouldShowBroken(loggedInUser: Option[User]): Boolean = {
    loggedInUser.exists { user =>
      user.isAdmin
    }
  }
}
