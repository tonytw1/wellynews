package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

@Component class HibernateResourceDAO @Autowired() (mongoRepository: MongoRepository) extends ReasonableWaits {

  def loadFeedByUrlWords(urlWords: String)(implicit ec: ExecutionContext): Option[Feed] = {
    Await.result(mongoRepository.getFeedByUrlwords(urlWords), TenSeconds)
  }

  @SuppressWarnings(Array("unchecked")) def getAllPublishersMatchingStem(stem: String, showBroken: Boolean): Seq[Resource] = {
    /*
    if (showBroken) {
      sessionFactory.getCurrentSession.createCriteria(classOf[Website]).
        add(Restrictions.sqlRestriction(" page like \"%" + stem + "%\" ")).addOrder(Order.asc("name")).list.asInstanceOf[List[Resource]]
    }
    else {
      sessionFactory.getCurrentSession.createCriteria(classOf[Website]).
        add(Restrictions.sqlRestriction(" page like \"%" + stem + "%\" ")).add(Restrictions.eq("httpStatus", 200)).addOrder(Order.asc("name")).list.asInstanceOf[List[Resource]]
    }
    */
    Seq() // TODO
  }

  @SuppressWarnings(Array("unchecked")) def getNewsitemsMatchingStem(stem: String): Seq[Resource] = {
    // sessionFactory.getCurrentSession.createCriteria(classOf[Newsitem]).add(Restrictions.sqlRestriction(" page like \"%" + stem + "%\" ")).addOrder(Order.asc("name")).list.asInstanceOf[List[Resource]]
    Seq() // TODO
  }

  def getOwnedByUserCount(user: User): Int = {
    // return (sessionFactory.getCurrentSession.createQuery("select count(*) from Resource where owner = " + user.getId).iterate.next.asInstanceOf[Long]).intValue
    0 // TODO
  }

  def loadNewsitemByHeadlineAndPublisherWithinLastMonth(name: String, publisher: Website): Resource = {
    // return sessionFactory.getCurrentSession.createCriteria(classOf[Newsitem]).add(Restrictions.eq("name", name)).add(Restrictions.eq("publisher", publisher)).setMaxResults(1).uniqueResult.asInstanceOf[Resource]
    null
  }

}
