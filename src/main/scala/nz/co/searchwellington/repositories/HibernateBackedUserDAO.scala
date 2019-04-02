package nz.co.searchwellington.repositories

import java.util.UUID

import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}

@Component class HibernateBackedUserDAO @Autowired() (mongoRepository: MongoRepository) {

  def saveUser(user: User) {
    //sessionFactory.getCurrentSession.saveOrUpdate(user)
  }

  def deleteUser(user: User) {
    // sessionFactory.getCurrentSession.delete(user)
  }

}