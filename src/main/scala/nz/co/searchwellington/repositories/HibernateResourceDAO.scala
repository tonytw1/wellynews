package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Deprecated
@Component class HibernateResourceDAO @Autowired() (mongoRepository: MongoRepository) extends ReasonableWaits {

  @SuppressWarnings(Array("unchecked")) def getNewsitemsMatchingStem(stem: String): Seq[Resource] = {
    // sessionFactory.getCurrentSession.createCriteria(classOf[Newsitem]).add(Restrictions.sqlRestriction(" page like \"%" + stem + "%\" ")).addOrder(Order.asc("name")).list.asInstanceOf[List[Resource]]
    Seq() // TODO
  }

  def getOwnedByUserCount(user: User): Int = {
    // return (sessionFactory.getCurrentSession.createQuery("select count(*) from Resource where owner = " + user.getId).iterate.next.asInstanceOf[Long]).intValue
    0 // TODO
  }

}
