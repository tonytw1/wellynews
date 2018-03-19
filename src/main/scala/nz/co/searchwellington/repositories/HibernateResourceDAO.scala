package nz.co.searchwellington.repositories

import java.util.Date

import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await
import scala.concurrent.duration._

@Component class HibernateResourceDAO @Autowired() (mongoRepository: MongoRepository) {

  def getPublisherNamesByStartingLetters(q: String): Seq[String] = {
    Await.result(mongoRepository.getWebsiteByNamePrefix(q), Duration(1, MINUTES)).map(p => p.title.getOrElse(""))
  }

  def getAllFeeds: Seq[Feed] = {
    Await.result(mongoRepository.getAllFeeds(), Duration(10, SECONDS))
  }

  def getFeedsToRead: Seq[Feed] = {
    getAllFeeds.filterNot(f => f.acceptance == "ignore").sortBy(_.getLastRead).reverse
  }

  def getAllWatchlists: Seq[Resource] = {
    Await.result(mongoRepository.getAllWatchlists(), Duration(10, SECONDS))
  }


  def loadResourceById(resourceID: Int): Option[Resource] = {
    Await.result(mongoRepository.getResourceById(resourceID), Duration(1, MINUTES))
  }

  def loadResourceByUrl(url: String): Option[Resource] = {
    Await.result(mongoRepository.getResourceByUrl(url), Duration(1, MINUTES))
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

  @SuppressWarnings(Array("unchecked")) def getOwnedBy(owner: User, maxItems: Int): Seq[Resource] = {
    // return sessionFactory.getCurrentSession.createCriteria(classOf[Resource]).add(Restrictions.eq("owner", owner)).addOrder(Order.desc("date")).addOrder(Order.desc("id")).setMaxResults(maxItems).list.asInstanceOf[List[Resource]]
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

  @SuppressWarnings(Array("unchecked")) def getNotCheckedSince(oneMonthAgo: Date, maxItems: Int): Seq[Resource] = {
    // sessionFactory.getCurrentSession.createCriteria(classOf[Resource]).add(Restrictions.lt("lastScanned", oneMonthAgo)).addOrder(Order.asc("lastScanned")).setMaxResults(maxItems).list.asInstanceOf[List[Resource]]
    Seq() // TODO
  }

  @SuppressWarnings(Array("unchecked")) def getNotCheckedSince(launchedDate: Date, lastScanned: Date, maxItems: Int): Seq[Resource] = {
    //return sessionFactory.getCurrentSession.createCriteria(classOf[Resource]).add(Restrictions.gt("liveTime", launchedDate)).add(Restrictions.lt("lastScanned", lastScanned)).addOrder(Order.asc("lastScanned")).setMaxResults(maxItems).list.asInstanceOf[List[Resource]]
    Seq() // TODO
  }

  @SuppressWarnings(Array("unchecked")) def getCommentFeedsToCheck(maxItems: Int): Seq[CommentFeed] = {
    // return sessionFactory.getCurrentSession.createCriteria(classOf[CommentFeed]).addOrder(Order.desc("lastRead")).setCacheable(false).setMaxResults(maxItems).list.asInstanceOf[List[CommentFeed]]
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

  def loadFeedByUrl(url: String): Feed = {
   // return sessionFactory.getCurrentSession.createCriteria(classOf[Feed]).add(Restrictions.eq("url", url)).uniqueResult.asInstanceOf[Feed]
    null
  }

  def loadCommentFeedByUrl(url: String): CommentFeed = {
    // return sessionFactory.getCurrentSession.createCriteria(classOf[CommentFeed]).add(Restrictions.eq("url", url)).setMaxResults(1).uniqueResult.asInstanceOf[CommentFeed]
    null
  }

  def loadDiscoveredFeedByUrl(url: String): DiscoveredFeed = {
    // return sessionFactory.getCurrentSession.createCriteria(classOf[DiscoveredFeed]).add(Restrictions.eq("url", url)).setMaxResults(1).setCacheable(true).uniqueResult.asInstanceOf[DiscoveredFeed]
    null
  }

  def saveResource(resource: Resource) {
    /*
    if (resource.getType == "N") {
      if ((resource.asInstanceOf[Newsitem]).getImage != null) {
        sessionFactory.getCurrentSession.saveOrUpdate((resource.asInstanceOf[Newsitem]).getImage)
      }
    }
    sessionFactory.getCurrentSession.saveOrUpdate(resource)
    if (resource.getType == "F") {
      sessionFactory.evictCollection("nz.co.searchwellington.model.WebsiteImpl.feeds")
    }
    */
    null
  }

  def saveDiscoveredFeed(discoveredFeed: DiscoveredFeed) {
    // sessionFactory.getCurrentSession.saveOrUpdate(discoveredFeed)
    null // TODO
  }

  def saveCommentFeed(commentFeed: CommentFeed) {
    // sessionFactory.getCurrentSession.saveOrUpdate(commentFeed)
    null // TODO
  }

  @SuppressWarnings(Array("unchecked")) def getTaggedResources(tag: Tag, max_newsitems: Int): Seq[Resource] = {
      // return sessionFactory.getCurrentSession.createCriteria(classOf[Resource]).createCriteria("tags").add(Restrictions.eq("id", tag.getId)).list.asInstanceOf[List[Resource]]
    Seq()
  }

  def deleteResource(resource: Resource) {
//    sessionFactory.getCurrentSession.delete(resource)
 //   sessionFactory.evictCollection("nz.co.searchwellington.model.WebsiteImpl.feeds")
  //  sessionFactory.evictCollection("nz.co.searchwellington.model.WebsiteImpl.watchlist")
   // sessionFactory.evictCollection("nz.co.searchwellington.model.DiscoveredFeed.references")
  }

  def getTagsMatchingKeywords(keywords: String): Seq[Tag] = {
    throw (new UnsupportedOperationException)
  }

  @SuppressWarnings(Array("unchecked")) def getResourcesWithTag(tag: Tag): Seq[Resource] = {
   // val taggedResources = sessionFactory.getCurrentSession.createCriteria(classOf[Resource]).addOrder(Order.desc("date")).addOrder(Order.desc("id")).createAlias("tags", "rt").add(Restrictions.eq("rt.id", tag.getId))
   // taggedResources.list.asInstanceOf[List[Resource]]
      Seq()
   }

}
