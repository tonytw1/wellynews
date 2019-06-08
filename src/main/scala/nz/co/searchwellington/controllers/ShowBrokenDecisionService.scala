package nz.co.searchwellington.controllers

import nz.co.searchwellington.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class ShowBrokenDecisionService @Autowired()(loggedInUserFilter: LoggedInUserFilter) {

  def shouldShowBroken: Boolean = {
    val loggedInUser: User = loggedInUserFilter.getLoggedInUser
    loggedInUser != null && loggedInUser.isAdmin
  }
}
