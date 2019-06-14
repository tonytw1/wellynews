package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.{RelatedTagsService, ShowBrokenDecisionService}
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.elasticsearch._
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.joda.time.Interval
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.bson.BSONObjectID
import uk.co.eelpieconsulting.common.geo.model.LatLong

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Component class ContentRetrievalService @Autowired()( resourceDAO: HibernateResourceDAO,
                                                       keywordSearchService: KeywordSearchService,
                                                       showBrokenDecisionService: ShowBrokenDecisionService,
                                                       tagDAO: TagDAO,  relatedTagsService: RelatedTagsService,
                                                       frontendResourceMapper: FrontendResourceMapper, elasticSearchIndexer: ElasticSearchIndexer,
                                                       mongoRepository: MongoRepository) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[ContentRetrievalService])

  val MAX_NEWSITEMS_TO_SHOW = 30
  val ALL_ITEMS = 1000

  def getAllPublishers(loggedInUser: Option[User]): Future[Seq[Website]] = {
    elasticSearchIndexer.getAllPublishers(loggedInUser).flatMap { ids =>
      log.info("Got " + ids.size + " publisher ids")
      Future.sequence(ids.map { id =>
        mongoRepository.getResourceByObjectId(BSONObjectID(id._1)).map(ro => ro.map(_.asInstanceOf[Website]))
      }).map(_.flatten)
    }
  }

  def getTopLevelTags: Seq[Tag] = {
    tagDAO.getTopLevelTags
  }

  def getNewsitemsMatchingKeywords(keywords: String, startIndex: Int, maxNewsitems: Int, loggedInUser: Option[User]): Seq[FrontendResource] = {
    val query = ResourceQuery(`type` = Some("N"), q = Some(keywords))
    Await.result(elasticSearchIndexer.getResources(query, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getTagNewsitemsMatchingKeywords(keywords: String, tag: Tag, startIndex: Int, maxItems: Int, loggedInUser: Option[User]): Seq[FrontendResource] = {
    val query = ResourceQuery(`type` = Some("N"), q = Some(keywords), tags = Some(Set(tag)))
    Await.result(elasticSearchIndexer.getResources(query, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getTagWatchlist(tag: Tag, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val taggedWebsites = ResourceQuery(`type` = Some("L"), tags = Some(Set(tag)))
    elasticSearchIndexer.getResources(taggedWebsites, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getTaggedFeeds(tag: Tag, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val taggedWebsites = ResourceQuery(`type` = Some("F"), tags = Some(Set(tag)))
    elasticSearchIndexer.getResources(taggedWebsites, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getGeocodedNewsitems(startIndex: Int, maxItems: Int, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    elasticSearchIndexer.getResources(geocodedNewsitems, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getGeocodedNewitemsCount(loggedInUser: Option[User]): Long = {
    Await.result(elasticSearchIndexer.getResources(geocodedNewsitems, loggedInUser = loggedInUser).map(_._2), TenSeconds)
  }

  def getNewsitemsNear(latLong: LatLong, radius: Double, startIndex: Int, maxNewsitems: Int, loggedInUser: Option[User]): Seq[FrontendResource] = {
    val withPagination = nearbyNewsitems(latLong, radius).copy(startIndex = startIndex, maxItems = maxNewsitems)
    Await.result(elasticSearchIndexer.getResources(withPagination, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getNewsitemsNearCount(latLong: LatLong, radius: Double, loggedInUser: Option[User]): Long = {
    Await.result(elasticSearchIndexer.getResources(nearbyNewsitems(latLong, radius), loggedInUser = loggedInUser).map(i => i._2), TenSeconds)
  }

  def getGeotaggedNewsitemsForTag(tag: Tag, maxItems: Int, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val geotaggedNewsitemsForTag = ResourceQuery(`type` = Some("N"), geocoded = Some(true), tags = Some(Set(tag)), maxItems = ALL_ITEMS)
    elasticSearchIndexer.getResources(geotaggedNewsitemsForTag, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getLatestNewsitems(maxItems: Int = MAX_NEWSITEMS_TO_SHOW, page: Int = 1, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    elasticSearchIndexer.getResources(ResourceQuery(`type` = Some("N"), maxItems = maxItems, startIndex = maxItems * (page - 1)), loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getNewsitemsForInterval(interval: Interval, loggedInUser: Option[User]): Seq[FrontendResource] = {
    val newsitemsForMonth = ResourceQuery(`type` = Some("N"), interval = Some(interval), maxItems = ALL_ITEMS)
    Await.result(elasticSearchIndexer.getResources(newsitemsForMonth, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getLatestWebsites(maxItems: Int, page: Int = 1, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    elasticSearchIndexer.getResources(ResourceQuery(`type` = Some("W"), maxItems = maxItems, startIndex = maxItems * (page - 1)), loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getOwnedBy(user: User, loggedInUser: Option[User]): Seq[FrontendResource] = {
    Await.result(elasticSearchIndexer.getResources(
      ResourceQuery(
        owner = Some(user._id),
        maxItems = MAX_NEWSITEMS_TO_SHOW
      ),
      loggedInUser = loggedInUser
    ).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getTaggedBy(user: User, loggedInUser: Option[User]): Seq[FrontendResource] = {
    Await.result(elasticSearchIndexer.getResources(
      ResourceQuery(
        taggingUser = Some(user._id),
        maxItems = MAX_NEWSITEMS_TO_SHOW
      ),
      loggedInUser = loggedInUser
    ).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getKeywordSearchFacets(keywords: String): Seq[TagContentCount] = {
    relatedTagsService.getKeywordSearchFacets(keywords, null) // TODO This is abit odd - it's the only facet one which comes through here.
  }

  def getWebsitesMatchingKeywords(keywords: String, tag: Tag, startIndex: Int, maxItems: Int, loggedInUser: Option[User]): Seq[FrontendResource] = {
    keywordSearchService.getWebsitesMatchingKeywords(keywords, showBrokenDecisionService.shouldShowBroken(loggedInUser), tag, startIndex, maxItems)
  }

  def getNewsitemsMatchingKeywordsCount(keywords: String, tag: Tag, loggedInUser: Option[User]): Int = {
    keywordSearchService.getNewsitemsMatchingKeywordsCount(keywords, showBrokenDecisionService.shouldShowBroken(loggedInUser), tag)
  }

  def getNewsitemsMatchingKeywordsCount(keywords: String, loggedInUser: Option[User]): Int = {
    keywordSearchService.getNewsitemsMatchingKeywordsCount(keywords, showBrokenDecisionService.shouldShowBroken(loggedInUser), null)
  }

  def getArchiveMonths(loggedInUser: Option[User]): Future[Seq[ArchiveLink]] = elasticSearchIndexer.getArchiveMonths(loggedInUser)

  def getArchiveCounts(loggedInUser: Option[User]): Future[Map[String, Long]] = elasticSearchIndexer.getArchiveCounts(loggedInUser)

  def getTaggedNewitemsCount(tag: Tag, loggedInUser: Option[User]): Future[Long] = getTaggedNewsitemsCount(tags = Set(tag), loggedInUser)

  def getTaggedNewsitems(tag: Tag, startIndex: Int = 0, maxItems: Int = MAX_NEWSITEMS_TO_SHOW, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    getTaggedNewsitems(tags = Set(tag), startIndex = startIndex, maxItems = maxItems, loggedInUser)
  }

  def getFeeds(acceptancePolicy: Option[FeedAcceptancePolicy] = None, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val allFeeds = ResourceQuery(`type` = Some("F"), maxItems = ALL_ITEMS)

    val withAcceptancePolicy = acceptancePolicy.map { a =>
      allFeeds.copy(feedAcceptancePolicy = Some(a))
    }.getOrElse(allFeeds)

    elasticSearchIndexer.getResources(withAcceptancePolicy, elasticSearchIndexer.byTitleAscending, loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getAllFeedsOrderedByLatestItemDate(loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val allFeeds = ResourceQuery(`type` = Some("F"))
    elasticSearchIndexer.getResources(allFeeds, elasticSearchIndexer.byFeedLatestFeedItemDate, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1)) // TODO order
  }

  def getTaggedNewsitemsCount(tags: Set[Tag], loggedInUser: Option[User]): Future[Long] = {
    val query = ResourceQuery(`type` = Some("N"), tags = Some(tags))
    elasticSearchIndexer.getResources(query, loggedInUser = loggedInUser).map(_._2)
  }

  def getTaggedNewsitems(tags: Set[Tag], startIndex: Int, maxItems: Int, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val query = ResourceQuery(`type` = Some("N"), tags = Some(tags), startIndex = startIndex, maxItems = maxItems)
    elasticSearchIndexer.getResources(query, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getTaggedWebsites(tags: Set[Tag], maxItems: Int, loggedInUser: Option[User]): Seq[FrontendResource] = { // TODO no usages
    val query = ResourceQuery(`type` = Some("W"), tags = Some(tags), maxItems = maxItems)
    Await.result(elasticSearchIndexer.getResources(query, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getTaggedWebsites(tag: Tag, maxItems: Int, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val taggedWebsites = ResourceQuery(`type` = Some("W"), tags = Some(Set(tag)), maxItems = maxItems)
    elasticSearchIndexer.getResources(taggedWebsites, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getPublisherNewsitemsCount(publisher: Website, loggedInUser: Option[User]): Long = {
    val publisherNewsitems = ResourceQuery(`type` = Some("N"), publisher = Some(publisher))
    Await.result(elasticSearchIndexer.getResources(publisherNewsitems, loggedInUser = loggedInUser), TenSeconds)._2      // TODO show broken
  }

  def getPublisherNewsitems(publisher: Website, maxItems: Int, startIndex: Int, loggedInUser: Option[User]): Seq[FrontendResource] = {
    val publisherNewsitems = ResourceQuery(`type` = Some("N"), publisher = Some(publisher), startIndex = startIndex, maxItems = maxItems)
    Await.result(elasticSearchIndexer.getResources(publisherNewsitems, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getPublisherFeeds(publisher: Website, loggedInUser: Option[User]): Seq[FrontendResource] = {
    val publisherFeeds = ResourceQuery(`type` = Some("F"), publisher = Some(publisher))
    Await.result(elasticSearchIndexer.getResources(publisherFeeds, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1)), TenSeconds)  }

  def getAllWatchlists(loggedInUser: Option[User]): Seq[FrontendResource] = {
    val allWatchlists = ResourceQuery(`type` = Some("L"))
    Await.result(elasticSearchIndexer.getResources(allWatchlists, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getPublisherWatchlist(publisher: Website, loggedInUser: Option[User]): Seq[FrontendResource] = {
    val publisherWatchlist = ResourceQuery(`type` = Some("L"), publisher = Some(publisher))
    Await.result(elasticSearchIndexer.getResources(publisherWatchlist, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getPublisherTagCombinerNewsitems(publisher: Website, tag: Tag, maxNewsitems: Int, loggedInUser: Option[User]): Seq[FrontendResource] = {
    val publisherTagCombiner = ResourceQuery(`type` = Some("N"), publisher = Some(publisher), tags = Some(Set(tag)))
    Await.result(elasticSearchIndexer.getResources(publisherTagCombiner, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getFeedworthyTags(loggedInUser: Option[User]): Seq[Tag] = {
    var feedworthTags: Seq[Tag]= Seq()
    import scala.collection.JavaConversions._
    for (tagContentCount <- relatedTagsService.getFeedworthyTags(showBrokenDecisionService.shouldShowBroken(loggedInUser))) {
      feedworthTags.add(tagContentCount.getTag)
    }
    feedworthTags
  }

  def getDiscoveredFeeds: Future[Seq[DiscoveredFeed]] = {
    mongoRepository.getAllDiscoveredFeeds()
  }

  def getTagNamesStartingWith(q: String): Seq[String] = {
    tagDAO.getTagNamesStartingWith(q)
  }

  def getPublisherNamesByStartingLetters(q: String): Seq[String] = {
    log.info("Get publishers starting with '" + q + "'")
    val results = Await.result(mongoRepository.getWebsiteByNamePrefix(q), TenSeconds).map(p => p.title.getOrElse(""))
    log.info("Got: " + results.size)
    results
  }

  def getOwnedByCount(loggedInUser: User): Int = {
    resourceDAO.getOwnedByUserCount(loggedInUser)
  }

  def getNewsPage(pathInfo: String): Option[FrontendResource] = {
    //elasticSearchBackedResourceDAO.getNewspage(pathInfo, showBrokenDecisionService.shouldShowBroken)
    None
  }

  def getFeaturedTags: Seq[Tag] = {
    tagDAO.getFeaturedTags
  }

  private def fetchByIds(ids: Seq[BSONObjectID]): Future[Seq[FrontendResource]] = {
    val eventualResources = Future.sequence{ ids.map { id =>
      mongoRepository.getResourceByObjectId(id)
    }}.map(_.flatten)

    eventualResources.map(rs => rs.map(r => frontendResourceMapper.createFrontendResourceFrom(r)))
  }

  private val geocodedNewsitems = ResourceQuery(`type` = Some("N"), geocoded = Some(true))

  private def nearbyNewsitems(latLong: LatLong, radius: Double) = ResourceQuery(`type` = Some("N"), circle = Some(Circle(latLong, radius)))

}
