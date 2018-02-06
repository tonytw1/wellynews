package nz.co.searchwellington.repositories

import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.hibernate.criterion.{Order, Restrictions}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class HibernateBackedUserDAO @Autowired() (mongoRepository: MongoRepository) {

  def getUserByOpenId(openId: String): User = {
    sessionFactory.getCurrentSession.createCriteria(classOf[User]).add(Restrictions.eq("openId", openId)).uniqueResult.asInstanceOf[User]
  }

  def getActiveUsers(): Seq[User] = {
    sessionFactory.getCurrentSession.createCriteria(classOf[User]).addOrder(Order.asc("profilename")).setCacheable(true).list.asInstanceOf[java.util.List[User]]
  }

  def saveUser(user: User) {
    sessionFactory.getCurrentSession.saveOrUpdate(user)
  }

  def getUserByProfileName(profilename: String): Option[User] = {
    Option(sessionFactory.getCurrentSession.createCriteria(classOf[User]).add(Restrictions.eq("profilename", profilename)).uniqueResult.asInstanceOf[User])
  }

  def getUserByTwitterId(twitterId: Long): User = {
    sessionFactory.getCurrentSession.createCriteria(classOf[User]).add(Restrictions.eq("twitterId", twitterId)).uniqueResult.asInstanceOf[User]
  }

  def getUserByApiKey(apiKey: String): User = {
    sessionFactory.getCurrentSession.createCriteria(classOf[User]).add(Restrictions.eq("apikey", apiKey)).uniqueResult.asInstanceOf[User]
  }

  def getNextAvailableAnonUserNumber: Int = {
    val iterate: java.util.Iterator[_] = sessionFactory.getCurrentSession.createQuery("select max(id) from UserImpl").iterate
    if (iterate != null && iterate.hasNext) {
      val next: Integer = iterate.next.asInstanceOf[Integer]
      if (next != null) {
        return next + 1
      }
    }
    return 1
  }

  def deleteUser(user: User) {
    sessionFactory.getCurrentSession.delete(user)
  }

}