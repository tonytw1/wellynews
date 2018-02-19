package nz.co.searchwellington.repositories

import java.util.Date

import nz.co.searchwellington.controllers.{RelatedTagsService, ShowBrokenDecisionService}
import nz.co.searchwellington.feeds.DiscoveredFeedRepository
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.frontend.{FrontendResource, FrontendTag}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.elasticsearch.{ElasticSearchBackedResourceDAO, ElasticSearchIndexer, KeywordSearchService, ResourceQuery}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.model.LatLong

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

@Component class ContentRetrievalService @Autowired()( resourceDAO: HibernateResourceDAO,
                                                       keywordSearchService: KeywordSearchService,  showBrokenDecisionService: ShowBrokenDecisionService,
                                                       tagDAO: TagDAO,  relatedTagsService: RelatedTagsService,
                                                       discoveredFeedsDAO: DiscoveredFeedRepository,
                                                       elasticSearchBackedResourceDAO: ElasticSearchBackedResourceDAO,
                                                       frontendResourceMapper: FrontendResourceMapper, elasticSearchIndexer: ElasticSearchIndexer,
                                                       mongoRepository: MongoRepository) {

  val MAX_NEWSITEMS_TO_SHOW = 30

  def getGeocoded(startIndex: Int, maxItems: Int): Seq[FrontendResource] = {
    elasticSearchBackedResourceDAO.getGeotagged(startIndex, maxItems, showBrokenDecisionService.shouldShowBroken)
  }

  def getGeotaggedCount: Long = {
    elasticSearchBackedResourceDAO.getGeotaggedCount(showBrokenDecisionService.shouldShowBroken)
  }

  def getAllPublishers: Seq[PublisherContentCount] = {
    elasticSearchIndexer.getAllPublishers()   // TODO show broken
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

  def getTagWatchlist(tag: Tag): Seq[FrontendResource] = {
    val taggedWebsites = ResourceQuery(`type` = Some("L"), tags = Some(Set(tag)))
    Await.result(elasticSearchIndexer.getResources(taggedWebsites).flatMap(i => fetchByIds(i._1)), Duration(10, SECONDS))
  }

  def getTaggedFeeds(tag: Tag): Seq[FrontendResource] = {
    val taggedWebsites = ResourceQuery(`type` = Some("F"), tags = Some(Set(tag)))
    Await.result(elasticSearchIndexer.getResources(taggedWebsites).flatMap(i => fetchByIds(i._1)), Duration(10, SECONDS))
  }

  def getTaggedGeotaggedNewsitems(tag: Tag, maxItems: Int): Seq[FrontendResource] = {
    elasticSearchBackedResourceDAO.getTaggedGeotaggedNewsitems(tag, maxItems, showBrokenDecisionService.shouldShowBroken)
  }

  def getNewsitemsNear(latLong: LatLong, radius: Double, startIndex: Int, maxNewsitems: Int): Seq[FrontendResource] = {
    elasticSearchBackedResourceDAO.getGeotaggedNewsitemsNear(latLong, radius, showBrokenDecisionService.shouldShowBroken, startIndex, maxNewsitems)
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

  def getCommentedNewsitems(maxItems: Int, startIndex: Int): Seq[FrontendResource] = {
    elasticSearchBackedResourceDAO.getCommentedNewsitems(maxItems, showBrokenDecisionService.shouldShowBroken, true, startIndex)
  }

  def getCommentedNewsitemsCount: Int = {
    elasticSearchBackedResourceDAO.getCommentedNewsitemsCount(showBrokenDecisionService.shouldShowBroken)
  }

  def getCommentedTags: Seq[Tag] = {
    elasticSearchBackedResourceDAO.getCommentedTags(showBrokenDecisionService.shouldShowBroken)
  }

  def getLatestNewsitems: Seq[FrontendResource] = {
    getLatestNewsitems(MAX_NEWSITEMS_TO_SHOW, 1)
  }

  def getLatestNewsitems(maxItems: Int, page: Int = 1): Seq[FrontendResource] = {
    Await.result(elasticSearchIndexer.getResources(ResourceQuery(`type` = Some("N"), maxItems = maxItems, startIndex = (maxItems * (page - 1)))).flatMap(i => fetchByIds(i._1)), Duration(10, SECONDS))
  }

  def getLatestWebsites(maxItems: Int, page: Int = 1): Seq[FrontendResource] = {
    Await.result(elasticSearchIndexer.getResources(ResourceQuery(`type` = Some("W"), maxItems = maxItems, startIndex = (maxItems * (page - 1)))).flatMap(i => fetchByIds(i._1)), Duration(10, SECONDS))
  }

  private def fetchByIds(ids: Seq[Int]): Future[Seq[FrontendResource]] = {
    val eventualResources = Future.sequence{ ids.map { id =>
      mongoRepository.getResourceById(id)
    }}.map(_.flatten)

    eventualResources.map(rs => rs.map(r => frontendResourceMapper.createFrontendResourceFrom(r)))
  }

  def getKeywordSearchFacets(keywords: String): Seq[TagContentCount] = {
    relatedTagsService.getKeywordSearchFacets(keywords, null) // TODO This is abit odd - it's the only facet one which comes through here.
  }

  def getWebsitesMatchingKeywords(keywords: String, tag: Tag, startIndex: Int, maxItems: Int): Seq[FrontendResource] = {
    keywordSearchService.getWebsitesMatchingKeywords(keywords, showBrokenDecisionService.shouldShowBroken, tag, startIndex, maxItems)
  }

  def getNewsitemsMatchingKeywords(keywords: String, startIndex: Int, maxNewsitems: Int): Seq[FrontendResource] = {
    keywordSearchService.getNewsitemsMatchingKeywords(keywords, showBrokenDecisionService.shouldShowBroken, null, startIndex, maxNewsitems)
  }

  def getNewsitemsMatchingKeywords(keywords: String, tag: Tag, startIndex: Int, maxItems: Int): Seq[FrontendResource] = {
    keywordSearchService.getNewsitemsMatchingKeywords(keywords, showBrokenDecisionService.shouldShowBroken, tag, startIndex, maxItems)
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
    elasticSearchBackedResourceDAO.getArchiveMonths(showBrokenDecisionService.shouldShowBroken)
  }

  def getArchiveStatistics: Map[String, Int] = {
    elasticSearchBackedResourceDAO.getArchiveStatistics(showBrokenDecisionService.shouldShowBroken)
  }

  def getCommentedNewsitemsForTag(tag: Tag, maxNewsitems: Int, startIndex: Int): Seq[FrontendResource] = {
    elasticSearchBackedResourceDAO.getCommentedNewsitemsForTag(tag, showBrokenDecisionService.shouldShowBroken, maxNewsitems, startIndex)
  }

  def getNewsitemsForMonth(month: Date): Seq[FrontendResource] = {
    elasticSearchBackedResourceDAO.getNewsitemsForMonth(month, showBrokenDecisionService.shouldShowBroken)
  }

  def getTaggedNewitemsCount(tag: Tag): Long = {
   getTaggedNewsitemsCount(tags = Set(tag))
  }

  def getTaggedNewsitems(tag: Tag, startIndex: Int = 0, maxItems: Int = MAX_NEWSITEMS_TO_SHOW): Seq[FrontendResource] = {
    getTaggedNewsitems(tags = Set(tag), startIndex = startIndex, maxItems = maxItems)
  }

  def getAllFeeds: Seq[FrontendResource] = {
    val allFeeds = ResourceQuery(`type` = Some("F"))
    Await.result(elasticSearchIndexer.getResources(allFeeds).flatMap(i => fetchByIds(i._1)), Duration(10, SECONDS))
  }

  def getAllFeedsOrderByLatestItemDate(): Seq[FrontendResource] = {
    val allFeeds = ResourceQuery(`type` = Some("F"))
    Await.result(elasticSearchIndexer.getResources(allFeeds).flatMap(i => fetchByIds(i._1)), Duration(10, SECONDS)) // TODO order
  }

  def getTaggedNewsitemsCount(tags: Set[Tag]): Long = {
    val query = ResourceQuery(`type` = Some("N"), tags = Some(tags))
    Await.result(elasticSearchIndexer.getResources(query), Duration(10, SECONDS))._2      // TODO show broken
  }

  def getTaggedNewsitems(tags: Set[Tag], startIndex: Int, maxItems: Int): Seq[FrontendResource] = {
    val query = ResourceQuery(`type` = Some("N"), tags = Some(tags), startIndex = startIndex, maxItems = maxItems)
    Await.result(elasticSearchIndexer.getResources(query).flatMap(i => fetchByIds(i._1)), Duration(10, SECONDS))
  }

  def getTaggedWebsites(tags: Set[Tag], maxItems: Int): Seq[FrontendResource] = { // TODO no usages
    val query = ResourceQuery(`type` = Some("W"), tags = Some(tags), maxItems = maxItems)
    Await.result(elasticSearchIndexer.getResources(query).flatMap(i => fetchByIds(i._1)), Duration(10, SECONDS))
  }

  def getTaggedWebsites(tag: Tag, maxItems: Int): Seq[FrontendResource] = {
    val taggedWebsites = ResourceQuery(`type` = Some("W"), tags = Some(Set(tag)), maxItems = maxItems)
    Await.result(elasticSearchIndexer.getResources(taggedWebsites).flatMap(i => fetchByIds(i._1)), Duration(10, SECONDS))
  }

  def getPublisherNewsitemsCount(publisher: Website): Long = {
    val publisherNewsitems = ResourceQuery(`type` = Some("N"), publisher = Some(publisher))
    Await.result(elasticSearchIndexer.getResources(publisherNewsitems), Duration(10, SECONDS))._2      // TODO show broken
  }

  def getPublisherNewsitems(publisher: Website, maxItems: Int, startIndex: Int): Seq[FrontendResource] = {
    val publisherNewsitems = ResourceQuery(`type` = Some("N"), publisher = Some(publisher), startIndex = startIndex, maxItems = maxItems)
    Await.result(elasticSearchIndexer.getResources(publisherNewsitems).flatMap(i => fetchByIds(i._1)), Duration(10, SECONDS))
  }

  def getPublisherFeeds(publisher: Website): Seq[FrontendResource] = {
    val publisherFeeds = ResourceQuery(`type` = Some("F"), publisher = Some(publisher))
    Await.result(elasticSearchIndexer.getResources(publisherFeeds).flatMap(i => fetchByIds(i._1)), Duration(10, SECONDS))  }

  def getAllWatchlists: Seq[FrontendResource] = {
    val allWatchlists = ResourceQuery(`type` = Some("L"))
    Await.result(elasticSearchIndexer.getResources(allWatchlists).flatMap(i => fetchByIds(i._1)), Duration(10, SECONDS))
  }

  def getPublisherWatchlist(publisher: Website): Seq[FrontendResource] = {
    val publisherWatchlist = ResourceQuery(`type` = Some("L"), publisher = Some(publisher))
    Await.result(elasticSearchIndexer.getResources(publisherWatchlist).flatMap(i => fetchByIds(i._1)), Duration(10, SECONDS))
  }

  def getPublisherTagCombinerNewsitems(publisher: Website, tag: Tag, maxNewsitems: Int): Seq[FrontendResource] = {
    elasticSearchBackedResourceDAO.getPublisherTagCombinerNewsitems(publisher, tag, showBrokenDecisionService.shouldShowBroken, maxNewsitems)
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

  def getOwnedBy(loggedInUser: User): Seq[FrontendResource] = {
    var owned: Seq[FrontendResource] = Seq()
    import scala.collection.JavaConversions._
    for (resource <- resourceDAO.getOwnedBy(loggedInUser, MAX_NEWSITEMS_TO_SHOW)) {
      owned.add(frontendResourceMapper.createFrontendResourceFrom(resource))
    }
    owned
  }

  def getTaggedBy(user: User): Seq[FrontendResource] = {
    elasticSearchBackedResourceDAO.getHandTaggingsForUser(user, showBrokenDecisionService.shouldShowBroken)
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

}
