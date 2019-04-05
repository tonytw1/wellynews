package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.{RelatedTagsService, ShowBrokenDecisionService}
import nz.co.searchwellington.feeds.DiscoveredFeedRepository
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.frontend.{FrontendResource, FrontendTag}
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
                                                       discoveredFeedsDAO: DiscoveredFeedRepository,
                                                       elasticSearchBackedResourceDAO: ElasticSearchBackedResourceDAO,
                                                       frontendResourceMapper: FrontendResourceMapper, elasticSearchIndexer: ElasticSearchIndexer,
                                                       mongoRepository: MongoRepository) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[ContentRetrievalService])

  val MAX_NEWSITEMS_TO_SHOW = 30
  val ALL_ITEMS = 1000

  def getGeotaggedCount: Long = {
    elasticSearchBackedResourceDAO.getGeotaggedCount(showBrokenDecisionService.shouldShowBroken)
  }

  def getAllPublishers: Future[Seq[Website]] = {
    elasticSearchIndexer.getAllPublishers().flatMap { ids =>
      log.info("Got " + ids.size + " publisher ids")
      Future.sequence(ids.map { id =>
        mongoRepository.getResourceByObjectId(BSONObjectID(id)).map(ro => ro.map(_.asInstanceOf[Website]))
      }).map(_.flatten)
    }
  }

  def getTopLevelTags: Seq[Tag] = {
    tagDAO.getTopLevelTags
  }

  def getCommentedNewsitemsForTagCount(tag: Tag): Int = {
    elasticSearchBackedResourceDAO.getCommentedNewsitemsForTagCount(tag, showBrokenDecisionService.shouldShowBroken)
  }

  def getRecentCommentedNewsitemsForTag(tag: Tag, maxItems: Int): Seq[FrontendResource] = {
    elasticSearchBackedResourceDAO.getRecentCommentedNewsitemsForTag(tag, showBrokenDecisionService.shouldShowBroken, maxItems)
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

  def getGeocoded(startIndex: Int, maxItems: Int): Seq[FrontendResource] = {
    val geotaggedNewsitems = ResourceQuery(`type` = Some("N"), geocoded = Some(true))
    Await.result(elasticSearchIndexer.getResources(geotaggedNewsitems).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getNewsitemsNear(latLong: LatLong, radius: Double, startIndex: Int, maxNewsitems: Int): Seq[FrontendResource] = {
    val nearbyNewsitems = ResourceQuery(`type` = Some("N"),
      startIndex = startIndex, maxItems = maxNewsitems,
      circle = Some(Circle(latLong, radius)))
    Await.result(elasticSearchIndexer.getResources(nearbyNewsitems).flatMap(i => fetchByIds(i._1)), TenSeconds)
  }

  def getTaggedGeotaggedNewsitems(tag: Tag, maxItems: Int): Seq[FrontendResource] = {
    elasticSearchBackedResourceDAO.getTaggedGeotaggedNewsitems(tag, maxItems, showBrokenDecisionService.shouldShowBroken)
  }

  def getNewsitemsNearDistanceFacet(latLong: LatLong): Map[Double, Long] = {
    elasticSearchBackedResourceDAO.getNewsitemsNearDistanceFacet(latLong, showBrokenDecisionService.shouldShowBroken)
  }

  def getNewsitemsNearCount(latLong: LatLong, radius: Double): Long = {
    elasticSearchBackedResourceDAO.getGeotaggedNewsitemsNearCount(latLong, radius, showBrokenDecisionService.shouldShowBroken)
  }

  def getGeotaggedTags: Seq[Tag] = {
    elasticSearchBackedResourceDAO.getGeotaggedTags(showBrokenDecisionService.shouldShowBroken)
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

  def getOwnedBy(loggedInUser: User): Seq[FrontendResource] = {
    Await.result(elasticSearchIndexer.getResources(
      ResourceQuery(
        maxItems = MAX_NEWSITEMS_TO_SHOW,
        owner = Some(loggedInUser._id)
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

  def getFeaturedSites: Seq[FrontendResource] = {
    tagDAO.loadTagByName("featured").map { featuredTag =>
      getTaggedWebsites(featuredTag, 10)
    }.getOrElse{
      Seq()
    }
  }

  def getArchiveMonths: Seq[ArchiveLink] = {
    Await.result(elasticSearchIndexer.getArchiveMonths, TenSeconds)
  }

  def getArchiveCounts: Map[String, Long] = {
    Await.result(elasticSearchIndexer.getArchiveCounts, TenSeconds)
  }

  def getCommentedNewsitemsForTag(tag: Tag, maxNewsitems: Int, startIndex: Int): Seq[FrontendResource] = {
    elasticSearchBackedResourceDAO.getCommentedNewsitemsForTag(tag, showBrokenDecisionService.shouldShowBroken, maxNewsitems, startIndex)
  }

  def getTaggedNewitemsCount(tag: Tag): Long = {
   getTaggedNewsitemsCount(tags = Set(tag))
  }

  def getTaggedNewsitems(tag: Tag, startIndex: Int = 0, maxItems: Int = MAX_NEWSITEMS_TO_SHOW): Seq[FrontendResource] = {
    getTaggedNewsitems(tags = Set(tag), startIndex = startIndex, maxItems = maxItems)
  }

  def getAllFeeds: Seq[FrontendResource] = {
    val allFeeds = ResourceQuery(`type` = Some("F"), maxItems = ALL_ITEMS)
    Await.result(elasticSearchIndexer.getResources(allFeeds).flatMap(i => fetchByIds(i._1)), TenSeconds).sortBy(_.getName)
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
    Seq.empty // TODO
    //elasticSearchBackedResourceDAO.getPublisherTagCombinerNewsitems(publisher, tag, showBrokenDecisionService.shouldShowBroken, maxNewsitems)
  }

  def getPublisherTagCombinerNewsitems(publisherUrlWords: String, tagName: String, maxNewsitems: Int): Seq[FrontendResource] = {
    resourceDAO.getPublisherByUrlWords(publisherUrlWords).flatMap { p =>
      tagDAO.loadTagByName(tagName).map { t =>
        getPublisherTagCombinerNewsitems(p, t, maxNewsitems)
      }
    }.getOrElse(Seq())
  }

  def getFeedworthyTags: Seq[FrontendTag] = {
    var feedworthTags: Seq[FrontendTag]= Seq()
    import scala.collection.JavaConversions._
    for (tagContentCount <- relatedTagsService.getFeedworthyTags(showBrokenDecisionService.shouldShowBroken)) {
      feedworthTags.add(tagContentCount.getTag)
    }
    feedworthTags
  }

  def getDiscoveredFeeds: Seq[DiscoveredFeed] = {
    discoveredFeedsDAO.getAllNonCommentDiscoveredFeeds
  }

  def getTaggedBy(user: User): Seq[FrontendResource] = {
    Seq.empty  // TODO
    // elasticSearchBackedResourceDAO.getHandTaggingsForUser(user, showBrokenDecisionService.shouldShowBroken)
  }

  def getTagNamesStartingWith(q: String): Seq[String] = {
    tagDAO.getTagNamesStartingWith(q)
  }

  def getPublisherNamesByStartingLetters(q: String): Seq[String] = {
    resourceDAO.getPublisherNamesByStartingLetters(q)
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

}
