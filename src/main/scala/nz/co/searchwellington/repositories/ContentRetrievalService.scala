package nz.co.searchwellington.repositories

import java.util.Date

import nz.co.searchwellington.controllers.{RelatedTagsService, ShowBrokenDecisionService}
import nz.co.searchwellington.feeds.DiscoveredFeedRepository
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.frontend.{FrontendResource, FrontendTag}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.elasticsearch.{ElasticSearchBackedResourceDAO, KeywordSearchService}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.model.LatLong

@Component class ContentRetrievalService @Autowired()(var resourceDAO: HibernateResourceDAO, var keywordSearchService: KeywordSearchService, var showBrokenDecisionService: ShowBrokenDecisionService, var tagDAO: TagDAO, var relatedTagsService: RelatedTagsService, var discoveredFeedsDAO: DiscoveredFeedRepository, var elasticSearchBackedResourceDAO: ElasticSearchBackedResourceDAO, var frontendResourceMapper: FrontendResourceMapper) {

  val MAX_NEWSITEMS_TO_SHOW = 30

  def getAllWatchlists: Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getAllWatchlists(showBrokenDecisionService.shouldShowBroken)
  }

  def getGeocoded(startIndex: Int, maxItems: Int): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getGeotagged(startIndex, maxItems, showBrokenDecisionService.shouldShowBroken)
  }

  def getGeotaggedCount: Long = {
    elasticSearchBackedResourceDAO.getGeotaggedCount(showBrokenDecisionService.shouldShowBroken)
  }

  def getAllPublishers: Seq[PublisherContentCount] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getAllPublishers(showBrokenDecisionService.shouldShowBroken)
  }

  def getTopLevelTags:Seq[Tag] = {
    tagDAO.getTopLevelTags
  }

  def getTaggedNewitemsCount(tag: Tag): Long = {
    elasticSearchBackedResourceDAO.getTaggedNewitemsCount(tag, showBrokenDecisionService.shouldShowBroken)
  }

  def getCommentedNewsitemsForTagCount(tag: Tag): Int = {
    elasticSearchBackedResourceDAO.getCommentedNewsitemsForTagCount(tag, showBrokenDecisionService.shouldShowBroken)
  }

  def getRecentCommentedNewsitemsForTag(tag: Tag, maxItems: Int): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getRecentCommentedNewsitemsForTag(tag, showBrokenDecisionService.shouldShowBroken, maxItems)
  }

  def getTagWatchlist(tag: Tag): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getTagWatchlist(tag, showBrokenDecisionService.shouldShowBroken)
  }

  def getTaggedFeeds(tag: Tag): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getTaggedFeeds(tag, showBrokenDecisionService.shouldShowBroken)
  }

  def getTaggedGeotaggedNewsitems(tag: Tag, maxItems: Int): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getTaggedGeotaggedNewsitems(tag, maxItems, showBrokenDecisionService.shouldShowBroken)
  }

  def getNewsitemsNear(latLong: LatLong, radius: Double, startIndex: Int, maxNewsitems: Int): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getGeotaggedNewsitemsNear(latLong, radius, showBrokenDecisionService.shouldShowBroken, startIndex, maxNewsitems)
  }

  def getNewsitemsNearDistanceFacet(latLong: LatLong): java.util.Map[java.lang.Double, java.lang.Long] = {
    elasticSearchBackedResourceDAO.getNewsitemsNearDistanceFacet(latLong, showBrokenDecisionService.shouldShowBroken)
  }

  def getNewsitemsNearCount(latLong: LatLong, radius: Double): Long = {
    elasticSearchBackedResourceDAO.getGeotaggedNewsitemsNearCount(latLong, radius, showBrokenDecisionService.shouldShowBroken)
  }

  def getGeotaggedTags: Seq[Tag] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getGeotaggedTags(showBrokenDecisionService.shouldShowBroken)
  }

  def getCommentedNewsitems(maxItems: Int, startIndex: Int): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getCommentedNewsitems(maxItems, showBrokenDecisionService.shouldShowBroken, true, startIndex)
  }

  def getCommentedNewsitemsCount: Int = {
    elasticSearchBackedResourceDAO.getCommentedNewsitemsCount(showBrokenDecisionService.shouldShowBroken)
  }

  def getCommentedTags: Seq[Tag] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getCommentedTags(showBrokenDecisionService.shouldShowBroken)
  }

  def getLatestNewsitems: Seq[FrontendResource] = {
    getLatestNewsitems(MAX_NEWSITEMS_TO_SHOW, 1)
  }

  def getLatestNewsitems(maxNumber: Int, page: Int): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getLatestNewsitems(maxNumber, showBrokenDecisionService.shouldShowBroken, (page - 1) * maxNumber)
  }

  def getLatestWebsites(maxItems: Int): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getLatestWebsites(maxItems, showBrokenDecisionService.shouldShowBroken)
  }

  def getKeywordSearchFacets(keywords: String): Seq[TagContentCount] = {
    import scala.collection.JavaConversions._
    relatedTagsService.getKeywordSearchFacets(keywords, null) // TODO This is abit odd - it's the only facet one which comes through here.
  }

  def getWebsitesMatchingKeywords(keywords: String, tag: Tag, startIndex: Int, maxItems: Int): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    keywordSearchService.getWebsitesMatchingKeywords(keywords, showBrokenDecisionService.shouldShowBroken, tag, startIndex, maxItems)
  }

  def getNewsitemsMatchingKeywords(keywords: String, startIndex: Int, maxNewsitems: Int): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    keywordSearchService.getNewsitemsMatchingKeywords(keywords, showBrokenDecisionService.shouldShowBroken, null, startIndex, maxNewsitems)
  }

  def getNewsitemsMatchingKeywords(keywords: String, tag: Tag, startIndex: Int, maxItems: Int): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
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
      this.getTaggedWebsites(featuredTag, 10)
    }.getOrElse{
      Seq()
    }
  }

  def getAllFeeds: Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getAllFeeds(showBrokenDecisionService.shouldShowBroken, false)
  }

  def getAllFeedsOrderByLatestItemDate: Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getAllFeeds(showBrokenDecisionService.shouldShowBroken, true)
  }

  def getPublisherFeeds(publisher: Website): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getPublisherFeeds(publisher, showBrokenDecisionService.shouldShowBroken)
  }

  def getPublisherNewsitemsCount(publisher: Website): Long = {
    elasticSearchBackedResourceDAO.getPublisherNewsitemsCount(publisher, showBrokenDecisionService.shouldShowBroken)
  }

  def getPublisherNewsitems(publisher: Website, maxItems: Int, startIndex: Int): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getPublisherNewsitems(publisher, maxItems, showBrokenDecisionService.shouldShowBroken, startIndex)
  }

  def getPublisherWatchlist(publisher: Website): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getPublisherWatchlist(publisher, showBrokenDecisionService.shouldShowBroken)
  }

  def getArchiveMonths: Seq[ArchiveLink] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getArchiveMonths(showBrokenDecisionService.shouldShowBroken)
  }

  def getArchiveStatistics: java.util.Map[String, Integer] = {
    elasticSearchBackedResourceDAO.getArchiveStatistics(showBrokenDecisionService.shouldShowBroken)
  }

  def getCommentedNewsitemsForTag(tag: Tag, maxNewsitems: Int, startIndex: Int): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getCommentedNewsitemsForTag(tag, showBrokenDecisionService.shouldShowBroken, maxNewsitems, startIndex)
  }

  def getNewsitemsForMonth(month: Date): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getNewsitemsForMonth(month, showBrokenDecisionService.shouldShowBroken)
  }

  def getTaggedWebsites(tags: Set[Tag], maxItems: Int): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getTaggedWebsites(tags, showBrokenDecisionService.shouldShowBroken, maxItems)
  }

  def getTaggedNewsitemsCount(tags: Seq[Tag]): Long = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getTaggedNewsitemsCount(tags, showBrokenDecisionService.shouldShowBroken)
  }

  def getTaggedNewsitems(tags: Seq[Tag], startIndex: Int, maxItems: Int): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getTaggedNewsitems(tags, showBrokenDecisionService.shouldShowBroken, startIndex, maxItems)
  }

  def getTaggedNewsitems(tag: Tag, startIndex: Int, maxNewsitems: Int): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getTaggedNewsitems(tag, showBrokenDecisionService.shouldShowBroken, startIndex, maxNewsitems)
  }

  def getTaggedWebsites(tag: Tag, maxItems: Int): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getTaggedWebsites(Set(tag), showBrokenDecisionService.shouldShowBroken, maxItems)
  }

  def getPublisherTagCombinerNewsitems(publisher: Website, tag: Tag, maxNewsitems: Int): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getPublisherTagCombinerNewsitems(publisher, tag, showBrokenDecisionService.shouldShowBroken, maxNewsitems)
  }

  def getPublisherTagCombinerNewsitems(publisherUrlWords: String, tagName: String, maxNewsitems: Int): Seq[FrontendResource] = {
    resourceDAO.getPublisherByUrlWords(publisherUrlWords).flatMap { p =>
      tagDAO.loadTagByName(tagName).map { t =>
        getPublisherTagCombinerNewsitems(p, t, maxNewsitems)
      }
    }.getOrElse(Seq())
  }

  def getRecentlyChangedWatchlistItems: Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getAllWatchlists(showBrokenDecisionService.shouldShowBroken)
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
    import scala.collection.JavaConversions._
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
    import scala.collection.JavaConversions._
    elasticSearchBackedResourceDAO.getHandTaggingsForUser(user, showBrokenDecisionService.shouldShowBroken)
  }

  def getTagNamesStartingWith(q: String): Seq[String] = {
    import scala.collection.JavaConversions._
    tagDAO.getTagNamesStartingWith(q)
  }

  def getPublisherNamesByStartingLetters(q: String): Seq[String] = {
    import scala.collection.JavaConversions._
    resourceDAO.getPublisherNamesByStartingLetters(q)
  }

  def getOwnedByCount(loggedInUser: User): Int = {
    resourceDAO.getOwnedByUserCount(loggedInUser)
  }

  def getNewsPage(pathInfo: String): FrontendResource = {
    elasticSearchBackedResourceDAO.getNewspage(pathInfo, showBrokenDecisionService.shouldShowBroken)
  }

  def getFeaturedTags: Seq[Tag] = {
    import scala.collection.JavaConversions._
    tagDAO.getFeaturedTags
  }

}
