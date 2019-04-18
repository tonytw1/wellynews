package nz.co.searchwellington.repositories

import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await
import scala.concurrent.duration._

@Component class HibernateResourceDAO @Autowired() (mongoRepository: MongoRepository) {

  def loadResourceById(resourceId: String): Option[Resource] = {
    Await.result(mongoRepository.getResourceById(resourceId), Duration(1, MINUTES))
  }

  @Deprecated // UI should be passing id
  def getPublisherByName(name: String): Option[Website] = {
    Await.result(mongoRepository.getWebsiteByName(name), Duration(1, MINUTES))

  }
  def getPublisherByUrlWords(urlWords: String): Option[Website] = {
    Await.result(mongoRepository.getWebsiteByUrlwords(urlWords), Duration(1, MINUTES))
  }

  def loadFeedByUrlWords(urlWords: String): Option[Feed] = {
    Await.result(mongoRepository.getFeedByUrlwords(urlWords), Duration(1, MINUTES))
  }

  @SuppressWarnings(Array("unchecked")) def getAllDiscoveredFeeds: Seq[DiscoveredFeed] = {
    //return sessionFactory.getCurrentSession.createCriteria(classOf[DiscoveredFeed]).setCacheable(true).addOrder(Order.desc("id")).list.asInstanceOf[List[DiscoveredFeed]]
    Seq() // TODO
  }

  @SuppressWarnings(Array("unchecked")) def getNewsitemsForFeed(feed: Feed): Seq[Newsitem] = {
    // return sessionFactory.getCurrentSession.createCriteria(classOf[Newsitem]).add(Restrictions.eq("feed", feed)).addOrder(Order.desc("date")).list.asInstanceOf[List[Newsitem]]
    Seq() // TODO
  }

  @SuppressWarnings(Array("unchecked")) def getNewsitemsForPublishers(publisher: Website): Seq[PublishedResource] = {
    // return sessionFactory.getCurrentSession.createCriteria(classOf[Newsitem]).add(Restrictions.eq("publisher", publisher)).list.asInstanceOf[List[PublishedResource]]
    Seq() // TODO
  }
  
  @SuppressWarnings(Array("unchecked")) def getRecentUntaggedNewsitems: Seq[Newsitem] = {
    //return sessionFactory.getCurrentSession.createCriteria(classOf[Newsitem]).add(Restrictions.isEmpty("tags")).add(Restrictions.eq("httpStatus", 200)).addOrder(Order.desc("date")).setMaxResults(12).setCacheable(true).list.asInstanceOf[List[Newsitem]]
    Seq() // TODO
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

  def loadByUrlWords(urlWords: String): Option[Resource] = {
    // Option(sessionFactory.getCurrentSession.createCriteria(classOf[Resource]).add(Restrictions.eq("urlWords", urlWords)).setMaxResults(1).uniqueResult.asInstanceOf[Resource])
    None  // TODO
  }

  def loadResourceByUniqueUrl(url: String): Option[Resource] = {
    // Option(sessionFactory.getCurrentSession.createCriteria(classOf[Resource]).add(Restrictions.eq("url", url)).uniqueResult.asInstanceOf[Resource])
    None // TODO
  }

  @SuppressWarnings(Array("unchecked")) def getTaggedResources(tag: Tag, max_newsitems: Int): Seq[Resource] = {
      // return sessionFactory.getCurrentSession.createCriteria(classOf[Resource]).createCriteria("tags").add(Restrictions.eq("id", tag.getId)).list.asInstanceOf[List[Resource]]
    Seq()
  }

}
