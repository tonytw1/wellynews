package nz.co.searchwellington.repositories

import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class HibernateBackedUserDAO @Autowired() (mongoRepository: MongoRepository) {

  def deleteUser(user: User) {
    // sessionFactory.getCurrentSession.delete(user)
  }

}