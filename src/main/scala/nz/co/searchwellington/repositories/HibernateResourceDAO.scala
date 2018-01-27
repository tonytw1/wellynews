package nz.co.searchwellington.repositories

import java.util.Date

import nz.co.searchwellington.model._
import org.hibernate.SessionFactory
import org.hibernate.criterion.{Order, Restrictions}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component class HibernateResourceDAO @Autowired() (sessionFactory: SessionFactory) {

  @SuppressWarnings(Array("unchecked")) def getAllResourceIds: Seq[Integer] = {
    val session = sessionFactory.getCurrentSession
    session.createQuery("select id from nz.co.searchwellington.model.ResourceImpl order by id DESC").setFetchSize(100).list().asInstanceOf[List[java.lang.Integer]]
  }

  def getPublisherNamesByStartingLetters(q: String): Seq[String] = {
    val session = sessionFactory.getCurrentSession
    session.createQuery("select name from nz.co.searchwellington.model.ResourceImpl where type='W' and name like ? order by name").setString(0, q + '%').setMaxResults(50).asInstanceOf[List[java.lang.String]]
  }

  @SuppressWarnings(Array("unchecked")) def getAllFeeds: Seq[Feed] = {
    return sessionFactory.getCurrentSession.createCriteria(classOf[Feed]).addOrder(Order.desc("latestItemDate")).addOrder(Order.asc("name")).setCacheable(true).list.asInstanceOf[List[Feed]]
  }

  @SuppressWarnings(Array("unchecked")) def getFeedsToRead: Seq[Feed] = {
    return sessionFactory.getCurrentSession.createCriteria(classOf[Feed]).add(Restrictions.ne("acceptancePolicy", "ignore")).addOrder(Order.asc("lastRead")).setCacheable(false).list.asInstanceOf[List[Feed]]
  }

  @SuppressWarnings(Array("unchecked")) def getAllWatchlists: Seq[Resource] = {
    return sessionFactory.getCurrentSession.createCriteria(classOf[Watchlist]).addOrder(Order.asc("name")).setCacheable(true).list.asInstanceOf[List[Resource]]
  }

  @SuppressWarnings(Array("unchecked")) def getAllDiscoveredFeeds: Seq[DiscoveredFeed] = {
    return sessionFactory.getCurrentSession.createCriteria(classOf[DiscoveredFeed]).setCacheable(true).addOrder(Order.desc("id")).list.asInstanceOf[List[DiscoveredFeed]]
  }

  @SuppressWarnings(Array("unchecked")) def getNewsitemsForFeed(feed: Feed): Seq[Newsitem] = {
    return sessionFactory.getCurrentSession.createCriteria(classOf[Newsitem]).add(Restrictions.eq("feed", feed)).addOrder(Order.desc("date")).list.asInstanceOf[List[Newsitem]]
  }

  @SuppressWarnings(Array("unchecked")) def getNewsitemsForPublishers(publisher: Website): Seq[PublishedResource] = {
    return sessionFactory.getCurrentSession.createCriteria(classOf[Newsitem]).add(Restrictions.eq("publisher", publisher)).list.asInstanceOf[List[PublishedResource]]
  }

  @SuppressWarnings(Array("unchecked")) def getOwnedBy(owner: User, maxItems: Int): Seq[Resource] = {
    return sessionFactory.getCurrentSession.createCriteria(classOf[Resource]).add(Restrictions.eq("owner", owner)).
      addOrder(Order.desc("date")).addOrder(Order.desc("id")).setMaxResults(maxItems).list.asInstanceOf[List[Resource]]
  }

  @SuppressWarnings(Array("unchecked")) def getRecentUntaggedNewsitems: Seq[Newsitem] = {
    return sessionFactory.getCurrentSession.createCriteria(classOf[Newsitem]).add(Restrictions.isEmpty("tags")).
      add(Restrictions.eq("httpStatus", 200)).addOrder(Order.desc("date")).setMaxResults(12).setCacheable(true).list.asInstanceOf[List[Newsitem]]
  }

  @SuppressWarnings(Array("unchecked")) def getAllPublishersMatchingStem(stem: String, showBroken: Boolean): Seq[Resource] = {
    if (showBroken) {
      sessionFactory.getCurrentSession.createCriteria(classOf[Website]).
        add(Restrictions.sqlRestriction(" page like \"%" + stem + "%\" ")).addOrder(Order.asc("name")).list.asInstanceOf[List[Resource]]
    }
    else {
      sessionFactory.getCurrentSession.createCriteria(classOf[Website]).
        add(Restrictions.sqlRestriction(" page like \"%" + stem + "%\" ")).add(Restrictions.eq("httpStatus", 200)).addOrder(Order.asc("name")).list.asInstanceOf[List[Resource]]
    }
  }

  @SuppressWarnings(Array("unchecked")) def getNewsitemsMatchingStem(stem: String): Seq[Resource] = {
    sessionFactory.getCurrentSession.createCriteria(classOf[Newsitem]).add(Restrictions.sqlRestriction(" page like \"%" + stem + "%\" ")).addOrder(Order.asc("name")).list.asInstanceOf[List[Resource]]
  }

  @SuppressWarnings(Array("unchecked")) def getNotCheckedSince(oneMonthAgo: Date, maxItems: Int): Seq[Resource] = {
    sessionFactory.getCurrentSession.createCriteria(classOf[Resource]).add(Restrictions.lt("lastScanned", oneMonthAgo)).addOrder(Order.asc("lastScanned")).setMaxResults(maxItems).list.asInstanceOf[List[Resource]]
  }

  @SuppressWarnings(Array("unchecked")) def getNotCheckedSince(launchedDate: Date, lastScanned: Date, maxItems: Int): Seq[Resource] = {
    return sessionFactory.getCurrentSession.createCriteria(classOf[Resource]).add(Restrictions.gt("liveTime", launchedDate)).
      add(Restrictions.lt("lastScanned", lastScanned)).addOrder(Order.asc("lastScanned")).setMaxResults(maxItems).list.asInstanceOf[List[Resource]]
  }

  @SuppressWarnings(Array("unchecked")) def getCommentFeedsToCheck(maxItems: Int): Seq[CommentFeed] = {
    return sessionFactory.getCurrentSession.createCriteria(classOf[CommentFeed]).addOrder(Order.desc("lastRead")).setCacheable(false).setMaxResults(maxItems).list.asInstanceOf[List[CommentFeed]]
  }

  def getOwnedByUserCount(user: User): Int = {
    return (sessionFactory.getCurrentSession.createQuery("select count(*) from ResourceImpl where owner = " + user.getId).iterate.next.asInstanceOf[Long]).intValue
  }

  def loadResourceById(resourceID: Int): Resource = {
    return sessionFactory.getCurrentSession.get(classOf[ResourceImpl], resourceID).asInstanceOf[Resource]
  }

  def loadResourceByUrl(url: String): Resource = {
    return sessionFactory.getCurrentSession.createCriteria(classOf[Resource]).add(Restrictions.eq("url", url)).setMaxResults(1).uniqueResult.asInstanceOf[Resource]
  }

  def loadNewsitemByHeadlineAndPublisherWithinLastMonth(name: String, publisher: Website): Resource = {
    return sessionFactory.getCurrentSession.createCriteria(classOf[Newsitem]).add(Restrictions.eq("name", name)).add(Restrictions.eq("publisher", publisher)).setMaxResults(1).uniqueResult.asInstanceOf[Resource]
  }

  def getPublisherByUrlWords(urlWords: String): Website = {
    return sessionFactory.getCurrentSession.createCriteria(classOf[Website]).add(Restrictions.eq("urlWords", urlWords)).setMaxResults(1).uniqueResult.asInstanceOf[Website]
  }

  def getPublisherByName(name: String): Website = {
    return sessionFactory.getCurrentSession.createCriteria(classOf[Website]).add(Restrictions.eq("name", name)).setMaxResults(1).uniqueResult.asInstanceOf[Website]
  }

  def loadFeedByUrlWords(urlWords: String): Feed = {
    return sessionFactory.getCurrentSession.createCriteria(classOf[Feed]).add(Restrictions.eq("urlWords", urlWords)).setMaxResults(1).uniqueResult.asInstanceOf[Feed]
  }

  def loadFeedByWhakaoroId(whakaoroId: String): Feed = {
    return sessionFactory.getCurrentSession.createCriteria(classOf[Feed]).add(Restrictions.eq("whakaokoId", whakaoroId)).setMaxResults(1).uniqueResult.asInstanceOf[Feed]
  }

  def loadByUrlWords(urlWords: String): Resource = {
    return sessionFactory.getCurrentSession.createCriteria(classOf[Resource]).add(Restrictions.eq("urlWords", urlWords)).setMaxResults(1).uniqueResult.asInstanceOf[Resource]
  }

  def loadResourceByUniqueUrl(url: String): Resource = {
    return sessionFactory.getCurrentSession.createCriteria(classOf[Resource]).add(Restrictions.eq("url", url)).uniqueResult.asInstanceOf[Resource]
  }

  def loadFeedByUrl(url: String): Feed = {
    return sessionFactory.getCurrentSession.createCriteria(classOf[Feed]).add(Restrictions.eq("url", url)).uniqueResult.asInstanceOf[Feed]
  }

  def loadCommentFeedByUrl(url: String): CommentFeed = {
    return sessionFactory.getCurrentSession.createCriteria(classOf[CommentFeed]).add(Restrictions.eq("url", url)).setMaxResults(1).uniqueResult.asInstanceOf[CommentFeed]
  }

  def loadDiscoveredFeedByUrl(url: String): DiscoveredFeed = {
    return sessionFactory.getCurrentSession.createCriteria(classOf[DiscoveredFeed]).add(Restrictions.eq("url", url)).setMaxResults(1).setCacheable(true).uniqueResult.asInstanceOf[DiscoveredFeed]
  }

  def saveResource(resource: Resource) {
    if (resource.getType == "N") {
      if ((resource.asInstanceOf[Newsitem]).getImage != null) {
        sessionFactory.getCurrentSession.saveOrUpdate((resource.asInstanceOf[Newsitem]).getImage)
      }
    }
    sessionFactory.getCurrentSession.saveOrUpdate(resource)
    if (resource.getType == "F") {
      sessionFactory.evictCollection("nz.co.searchwellington.model.WebsiteImpl.feeds")
    }
  }

  def saveDiscoveredFeed(discoveredFeed: DiscoveredFeed) {
    sessionFactory.getCurrentSession.saveOrUpdate(discoveredFeed)
  }

  def saveCommentFeed(commentFeed: CommentFeed) {
    sessionFactory.getCurrentSession.saveOrUpdate(commentFeed)
  }

  @SuppressWarnings(Array("unchecked")) def getTaggedResources(tag: Tag, max_newsitems: Int): Seq[Resource] = {
    return sessionFactory.getCurrentSession.createCriteria(classOf[Resource]).createCriteria("tags").add(Restrictions.eq("id", tag.getId)).list.asInstanceOf[List[Resource]]
  }

  def deleteResource(resource: Resource) {
    sessionFactory.getCurrentSession.delete(resource)
    sessionFactory.evictCollection("nz.co.searchwellington.model.WebsiteImpl.feeds")
    sessionFactory.evictCollection("nz.co.searchwellington.model.WebsiteImpl.watchlist")
    sessionFactory.evictCollection("nz.co.searchwellington.model.DiscoveredFeed.references")
  }

  def getTagsMatchingKeywords(keywords: String): Seq[Tag] = {
    throw (new UnsupportedOperationException)
  }

  @SuppressWarnings(Array("unchecked")) def getResourcesWithTag(tag: Tag): Seq[Resource] = {
    val taggedResources = sessionFactory.getCurrentSession.createCriteria(classOf[Resource]).addOrder(Order.desc("date")).addOrder(Order.desc("id")).createAlias("tags", "rt").add(Restrictions.eq("rt.id", tag.getId))
    taggedResources.list.asInstanceOf[List[Resource]]
  }

}