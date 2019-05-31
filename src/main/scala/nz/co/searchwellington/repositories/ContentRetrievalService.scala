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

  def getAllPublishers: Future[Seq[Website]] = {
    elasticSearchIndexer.getAllPublishers.flatMap { ids =>
      log.info("Got " + ids.size + " publisher ids")
      Future.sequence(ids.map { id =>
        mongoRepository.getResourceByObjectId(BSONObjectID(id._1)).map(ro => ro.map(_.asInstanceOf[Website]))
      }).map(_.flatten)
    }
  }

  def getTopLevelTags: Seq[Tag] = {
    tagDAO.getTopLevelTags
  }

  def getNewsitemsMatchingKeywords(keywords: String, startIndex: Int, maxNewsitems: Int): Seq[FrontendResource] = {
    val query = ResourceQuery(`type` = Some("N"), q = Some(keywords))
    Await.result(elasticSearchIndexer.getResources(query).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getTagNewsitemsMatchingKeywords(keywords: String, tag: Tag, startIndex: Int, maxItems: Int): Seq[FrontendResource] = {
    val query = ResourceQuery(`type` = Some("N"), q = Some(keywords), tags = Some(Set(tag)))
    Await.result(elasticSearchIndexer.getResources(query).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getTagWatchlist(tag: Tag): Seq[FrontendResource] = {
    val taggedWebsites = ResourceQuery(`type` = Some("L"), tags = Some(Set(tag)))
    Await.result(elasticSearchIndexer.getResources(taggedWebsites).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getTaggedFeeds(tag: Tag): Seq[FrontendResource] = {
    val taggedWebsites = ResourceQuery(`type` = Some("F"), tags = Some(Set(tag)))
    Await.result(elasticSearchIndexer.getResources(taggedWebsites).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getGeocodedNewsitems(startIndex: Int, maxItems: Int): Seq[FrontendResource] = {
    Await.result(elasticSearchIndexer.getResources(geocodedNewsitems).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getGeocodedNewitemsCount: Long = {
    Await.result(elasticSearchIndexer.getResources(geocodedNewsitems).map(_._2), TenSeconds)
  }

  def getNewsitemsNear(latLong: LatLong, radius: Double, startIndex: Int, maxNewsitems: Int): Seq[FrontendResource] = {
    val withPagination = nearbyNewsitems(latLong, radius).copy(startIndex = startIndex, maxItems = maxNewsitems)
    Await.result(elasticSearchIndexer.getResources(withPagination).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getNewsitemsNearCount(latLong: LatLong, radius: Double): Long = {
    Await.result(elasticSearchIndexer.getResources(nearbyNewsitems(latLong, radius)).map(i => i._2), TenSeconds)
  }

  def getGeotaggedNewsitemsForTag(tag: Tag, maxItems: Int): Seq[FrontendResource] = {
    val geotaggedNewsitemsForTag = ResourceQuery(`type` = Some("N"), geocoded = Some(true), tags = Some(Set(tag)), maxItems = ALL_ITEMS)
    Await.result(elasticSearchIndexer.getResources(geotaggedNewsitemsForTag).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getGeotaggedTags: Seq[TagContentCount] = {
    relatedTagsService.getGeocodedTagsAggregation
  }

  def getLatestNewsitems: Seq[FrontendResource] = {
    getLatestNewsitems(MAX_NEWSITEMS_TO_SHOW, 1)
  }

  def getLatestNewsitems(maxItems: Int, page: Int = 1): Seq[FrontendResource] = {
    Await.result(elasticSearchIndexer.getResources(ResourceQuery(`type` = Some("N"), maxItems = maxItems, startIndex = (maxItems * (page - 1)))).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getNewsitemsForInterval(interval: Interval): Seq[FrontendResource] = {
    val newsitemsForMonth = ResourceQuery(`type` = Some("N"), interval = Some(interval), maxItems = ALL_ITEMS)
    Await.result(elasticSearchIndexer.getResources(newsitemsForMonth).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getLatestWebsites(maxItems: Int, page: Int = 1): Seq[FrontendResource] = {
    Await.result(elasticSearchIndexer.getResources(ResourceQuery(`type` = Some("W"), maxItems = maxItems, startIndex = (maxItems * (page - 1)))).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getOwnedBy(user: User): Seq[FrontendResource] = {
    Await.result(elasticSearchIndexer.getResources(
      ResourceQuery(
        owner = Some(user._id),
        maxItems = MAX_NEWSITEMS_TO_SHOW
      )
    ).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getTaggedBy(user: User): Seq[FrontendResource] = {
    Await.result(elasticSearchIndexer.getResources(
      ResourceQuery(
        taggingUser = Some(user._id),
        maxItems = MAX_NEWSITEMS_TO_SHOW
      )
    ).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getKeywordSearchFacets(keywords: String): Seq[TagContentCount] = {
    relatedTagsService.getKeywordSearchFacets(keywords, null) // TODO This is abit odd - it's the only facet one which comes through here.
  }

  def getWebsitesMatchingKeywords(keywords: String, tag: Tag, startIndex: Int, maxItems: Int): Seq[FrontendResource] = {
    keywordSearchService.getWebsitesMatchingKeywords(keywords, showBrokenDecisionService.shouldShowBroken, tag, startIndex, maxItems)
  }

  def getNewsitemsMatchingKeywordsCount(keywords: String, tag: Tag): Int = {
    keywordSearchService.getNewsitemsMatchingKeywordsCount(keywords, showBrokenDecisionService.shouldShowBroken, tag)
  }

  def getNewsitemsMatchingKeywordsCount(keywords: String): Int = {
    keywordSearchService.getNewsitemsMatchingKeywordsCount(keywords, showBrokenDecisionService.shouldShowBroken, null)
  }

  def getArchiveMonths: Seq[ArchiveLink] = {
    Await.result(elasticSearchIndexer.getArchiveMonths, TenSeconds)
  }

  def getArchiveCounts: Map[String, Long] = {
    Await.result(elasticSearchIndexer.getArchiveCounts, TenSeconds)
  }

  def getTaggedNewitemsCount(tag: Tag): Long = {
   getTaggedNewsitemsCount(tags = Set(tag))
  }

  def getTaggedNewsitems(tag: Tag, startIndex: Int = 0, maxItems: Int = MAX_NEWSITEMS_TO_SHOW): Seq[FrontendResource] = {
    getTaggedNewsitems(tags = Set(tag), startIndex = startIndex, maxItems = maxItems)
  }

  def getFeeds(acceptancePolicy: Option[FeedAcceptancePolicy] = None): Seq[FrontendResource] = {
    val allFeeds = ResourceQuery(`type` = Some("F"), maxItems = ALL_ITEMS)

    val withAcceptancePolicy = acceptancePolicy.map { a =>
      allFeeds.copy(feedAcceptancePolicy = Some(a))
    }.getOrElse(allFeeds)

    Await.result(elasticSearchIndexer.getResources(withAcceptancePolicy).flatMap(i => fetchByIds(i._1)), TenSeconds).sortBy(_.getName)
  }

  def getAllFeedsOrderByLatestItemDate(): Seq[FrontendResource] = {
    val allFeeds = ResourceQuery(`type` = Some("F"))
    Await.result(elasticSearchIndexer.getResources(allFeeds).flatMap(i => fetchByIds(i._1)), TenSeconds) // TODO order
  }

  def getTaggedNewsitemsCount(tags: Set[Tag]): Long = {
    val query = ResourceQuery(`type` = Some("N"), tags = Some(tags))
    Await.result(elasticSearchIndexer.getResources(query), TenSeconds)._2      // TODO show broken
  }

  def getTaggedNewsitems(tags: Set[Tag], startIndex: Int, maxItems: Int): Seq[FrontendResource] = {
    val query = ResourceQuery(`type` = Some("N"), tags = Some(tags), startIndex = startIndex, maxItems = maxItems)
    Await.result(elasticSearchIndexer.getResources(query).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getTaggedWebsites(tags: Set[Tag], maxItems: Int): Seq[FrontendResource] = { // TODO no usages
    val query = ResourceQuery(`type` = Some("W"), tags = Some(tags), maxItems = maxItems)
    Await.result(elasticSearchIndexer.getResources(query).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getTaggedWebsites(tag: Tag, maxItems: Int): Seq[FrontendResource] = {
    val taggedWebsites = ResourceQuery(`type` = Some("W"), tags = Some(Set(tag)), maxItems = maxItems)
    Await.result(elasticSearchIndexer.getResources(taggedWebsites).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getPublisherNewsitemsCount(publisher: Website): Long = {
    val publisherNewsitems = ResourceQuery(`type` = Some("N"), publisher = Some(publisher))
    Await.result(elasticSearchIndexer.getResources(publisherNewsitems), TenSeconds)._2      // TODO show broken
  }

  def getPublisherNewsitems(publisher: Website, maxItems: Int, startIndex: Int): Seq[FrontendResource] = {
    val publisherNewsitems = ResourceQuery(`type` = Some("N"), publisher = Some(publisher), startIndex = startIndex, maxItems = maxItems)
    Await.result(elasticSearchIndexer.getResources(publisherNewsitems).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getPublisherFeeds(publisher: Website): Seq[FrontendResource] = {
    val publisherFeeds = ResourceQuery(`type` = Some("F"), publisher = Some(publisher))
    Await.result(elasticSearchIndexer.getResources(publisherFeeds).flatMap(i => fetchByIds(i._1)), TenSeconds)  }

  def getAllWatchlists: Seq[FrontendResource] = {
    val allWatchlists = ResourceQuery(`type` = Some("L"))
    Await.result(elasticSearchIndexer.getResources(allWatchlists).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getPublisherWatchlist(publisher: Website): Seq[FrontendResource] = {
    val publisherWatchlist = ResourceQuery(`type` = Some("L"), publisher = Some(publisher))
    Await.result(elasticSearchIndexer.getResources(publisherWatchlist).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getPublisherTagCombinerNewsitems(publisher: Website, tag: Tag, maxNewsitems: Int): Seq[FrontendResource] = {
    val publisherTagCombiner = ResourceQuery(`type` = Some("N"), publisher = Some(publisher), tags = Some(Set(tag)))
    Await.result(elasticSearchIndexer.getResources(publisherTagCombiner).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getFeedworthyTags: Seq[Tag] = {
    var feedworthTags: Seq[Tag]= Seq()
    import scala.collection.JavaConversions._
    for (tagContentCount <- relatedTagsService.getFeedworthyTags(showBrokenDecisionService.shouldShowBroken)) {
      feedworthTags.add(tagContentCount.getTag)
    }
    feedworthTags
  }

  def getDiscoveredFeeds: Seq[DiscoveredFeed] = {
    Await.result(mongoRepository.getAllDiscoveredFeeds(), TenSeconds)
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
