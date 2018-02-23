package nz.co.searchwellington.controllers

import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.HibernateBackedUserDAO
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class AnonUserService @Autowired() (userDAO: HibernateBackedUserDAO) {

  private val log = Logger.getLogger(classOf[AnonUserService])

  def createAnonUser: User = {
    val userNumber = userDAO.getNextAvailableAnonUserNumber
    val anonUser = User(0)
    //anonUser.setProfilename("anon" + userNumber)
    //log.info("Created new anon user: " + anonUser.getProfilename)
    userDAO.saveUser(anonUser)
    anonUser
  }

}