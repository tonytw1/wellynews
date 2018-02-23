package nz.co.searchwellington.repositories

import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}

@Component class HibernateBackedUserDAO @Autowired() (mongoRepository: MongoRepository) {

  def getUserByOpenId(openId: String): User = {
    //sessionFactory.getCurrentSession.createCriteria(classOf[User]).add(Restrictions.eq("openId", openId)).uniqueResult.asInstanceOf[User]
    null // TODO
  }

  def getActiveUsers(): Seq[User] = {
    //sessionFactory.getCurrentSession.createCriteria(classOf[User]).addOrder(Order.asc("profilename")).setCacheable(true).list.asInstanceOf[java.util.List[User]]
    Seq() // TODO
  }

  def saveUser(user: User) {
    //sessionFactory.getCurrentSession.saveOrUpdate(user)
  }

  def getUserByProfileName(profilename: String): Option[User] = {
    //Option(sessionFactory.getCurrentSession.createCriteria(classOf[User]).add(Restrictions.eq("profilename", profilename)).uniqueResult.asInstanceOf[User])
    None // TODO
  }

  def getUserByTwitterId(twitterId: Long): Option[User] = {
    Await.result(mongoRepository.getUserByTwitterId(twitterId),  Duration(10, SECONDS))
  }

  def getUserByApiKey(apiKey: String): User = {
    //sessionFactory.getCurrentSession.createCriteria(classOf[User]).add(Restrictions.eq("apikey", apiKey)).uniqueResult.asInstanceOf[User]
    null // TODO
  }

  def getNextAvailableAnonUserNumber: Int = {
    /*
    val iterate: java.util.Iterator[_] = sessionFactory.getCurrentSession.createQuery("select max(id) from UserImpl").iterate
    if (iterate != null && iterate.hasNext) {
      val next: Integer = iterate.next.asInstanceOf[Integer]
      if (next != null) {
        return next + 1
      }
    }
    */
    1 // TOOD
  }

  def deleteUser(user: User) {
    // sessionFactory.getCurrentSession.delete(user)
  }

}