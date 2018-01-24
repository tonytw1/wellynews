package nz.co.searchwellington.repositories

import java.util
import java.util.Date

import com.google.common.collect.Lists
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

  def getAllWatchlists: util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getAllWatchlists(showBrokenDecisionService.shouldShowBroken)
  }

  def getGeocoded(startIndex: Int, maxItems: Int): util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getGeotagged(startIndex, maxItems, showBrokenDecisionService.shouldShowBroken)
  }

  def getGeotaggedCount: Long = {
    elasticSearchBackedResourceDAO.getGeotaggedCount(showBrokenDecisionService.shouldShowBroken)
  }

  def getAllPublishers: util.List[PublisherContentCount] = {
    elasticSearchBackedResourceDAO.getAllPublishers(showBrokenDecisionService.shouldShowBroken)
  }

  def getTopLevelTags: util.List[Tag] = {
    tagDAO.getTopLevelTags
  }

  def getTaggedNewitemsCount(tag: Tag): Long = {
    elasticSearchBackedResourceDAO.getTaggedNewitemsCount(tag, showBrokenDecisionService.shouldShowBroken)
  }

  def getCommentedNewsitemsForTagCount(tag: Tag): Int = {
    elasticSearchBackedResourceDAO.getCommentedNewsitemsForTagCount(tag, showBrokenDecisionService.shouldShowBroken)
  }

  def getRecentCommentedNewsitemsForTag(tag: Tag, maxItems: Int): util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getRecentCommentedNewsitemsForTag(tag, showBrokenDecisionService.shouldShowBroken, maxItems)
  }

  def getTagWatchlist(tag: Tag): util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getTagWatchlist(tag, showBrokenDecisionService.shouldShowBroken)
  }

  def getTaggedFeeds(tag: Tag): util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getTaggedFeeds(tag, showBrokenDecisionService.shouldShowBroken)
  }

  def getTaggedGeotaggedNewsitems(tag: Tag, maxItems: Int): util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getTaggedGeotaggedNewsitems(tag, maxItems, showBrokenDecisionService.shouldShowBroken)
  }

  def getNewsitemsNear(latLong: LatLong, radius: Double, startIndex: Int, maxNewsitems: Int): util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getGeotaggedNewsitemsNear(latLong, radius, showBrokenDecisionService.shouldShowBroken, startIndex, maxNewsitems)
  }

  def getNewsitemsNearDistanceFacet(latLong: LatLong): util.Map[java.lang.Double, java.lang.Long] = {
    elasticSearchBackedResourceDAO.getNewsitemsNearDistanceFacet(latLong, showBrokenDecisionService.shouldShowBroken)
  }

  def getNewsitemsNearCount(latLong: LatLong, radius: Double): Long = {
    elasticSearchBackedResourceDAO.getGeotaggedNewsitemsNearCount(latLong, radius, showBrokenDecisionService.shouldShowBroken)
  }

  def getGeotaggedTags: util.List[Tag] = {
    elasticSearchBackedResourceDAO.getGeotaggedTags(showBrokenDecisionService.shouldShowBroken)
  }

  def getCommentedNewsitems(maxItems: Int, startIndex: Int): util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getCommentedNewsitems(maxItems, showBrokenDecisionService.shouldShowBroken, true, startIndex)
  }

  def getCommentedNewsitemsCount: Int = {
    elasticSearchBackedResourceDAO.getCommentedNewsitemsCount(showBrokenDecisionService.shouldShowBroken)
  }

  def getCommentedTags: util.List[Tag] = {
    elasticSearchBackedResourceDAO.getCommentedTags(showBrokenDecisionService.shouldShowBroken)
  }

  def getLatestNewsitems: util.List[FrontendResource] = {
    getLatestNewsitems(MAX_NEWSITEMS_TO_SHOW, 1)
  }

  def getLatestNewsitems(maxNumber: Int, page: Int): util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getLatestNewsitems(maxNumber, showBrokenDecisionService.shouldShowBroken, (page - 1) * maxNumber)
  }

  def getLatestWebsites(maxItems: Int): util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getLatestWebsites(maxItems, showBrokenDecisionService.shouldShowBroken)
  }

  def getKeywordSearchFacets(keywords: String): util.List[TagContentCount] = {
    relatedTagsService.getKeywordSearchFacets(keywords, null) // TODO This is abit odd - it's the only facet one which comes through here.
  }

  def getWebsitesMatchingKeywords(keywords: String, tag: Tag, startIndex: Int, maxItems: Int): util.List[FrontendResource] = {
    keywordSearchService.getWebsitesMatchingKeywords(keywords, showBrokenDecisionService.shouldShowBroken, tag, startIndex, maxItems)
  }

  def getNewsitemsMatchingKeywords(keywords: String, startIndex: Int, maxNewsitems: Int): util.List[FrontendResource] = {
    keywordSearchService.getNewsitemsMatchingKeywords(keywords, showBrokenDecisionService.shouldShowBroken, null, startIndex, maxNewsitems)
  }

  def getNewsitemsMatchingKeywords(keywords: String, tag: Tag, startIndex: Int, maxItems: Int): util.List[FrontendResource] = {
    keywordSearchService.getNewsitemsMatchingKeywords(keywords, showBrokenDecisionService.shouldShowBroken, tag, startIndex, maxItems)
  }

  def getNewsitemsMatchingKeywordsCount(keywords: String, tag: Tag): Int = {
    keywordSearchService.getNewsitemsMatchingKeywordsCount(keywords, showBrokenDecisionService.shouldShowBroken, tag)
  }

  def getNewsitemsMatchingKeywordsCount(keywords: String): Int = {
    keywordSearchService.getNewsitemsMatchingKeywordsCount(keywords, showBrokenDecisionService.shouldShowBroken, null)
  }

  def getFeaturedSites: util.List[FrontendResource] = {
    val featuredTag = tagDAO.loadTagByName("featured")
    if (featuredTag != null) {
      this.getTaggedWebsites(featuredTag, 10)
    } else {
      null
    }
  }

  def getAllFeeds: util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getAllFeeds(showBrokenDecisionService.shouldShowBroken, false)
  }

  def getAllFeedsOrderByLatestItemDate: util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getAllFeeds(showBrokenDecisionService.shouldShowBroken, true)
  }

  def getPublisherFeeds(publisher: Website): util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getPublisherFeeds(publisher, showBrokenDecisionService.shouldShowBroken)
  }

  def getPublisherNewsitemsCount(publisher: Website): Long = {
    elasticSearchBackedResourceDAO.getPublisherNewsitemsCount(publisher, showBrokenDecisionService.shouldShowBroken)
  }

  def getPublisherNewsitems(publisher: Website, maxItems: Int, startIndex: Int): util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getPublisherNewsitems(publisher, maxItems, showBrokenDecisionService.shouldShowBroken, startIndex)
  }

  def getPublisherWatchlist(publisher: Website): util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getPublisherWatchlist(publisher, showBrokenDecisionService.shouldShowBroken)
  }

  def getArchiveMonths: util.List[ArchiveLink] = {
    elasticSearchBackedResourceDAO.getArchiveMonths(showBrokenDecisionService.shouldShowBroken)
  }

  def getArchiveStatistics: util.Map[String, Integer] = {
    elasticSearchBackedResourceDAO.getArchiveStatistics(showBrokenDecisionService.shouldShowBroken)
  }

  def getCommentedNewsitemsForTag(tag: Tag, maxNewsitems: Int, startIndex: Int): util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getCommentedNewsitemsForTag(tag, showBrokenDecisionService.shouldShowBroken, maxNewsitems, startIndex)
  }

  def getNewsitemsForMonth(month: Date): util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getNewsitemsForMonth(month, showBrokenDecisionService.shouldShowBroken)
  }

  def getTaggedWebsites(tags: util.Set[Tag], maxItems: Int): util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getTaggedWebsites(tags, showBrokenDecisionService.shouldShowBroken, maxItems)
  }

  def getTaggedNewsitemsCount(tags: util.List[Tag]): Long = {
    elasticSearchBackedResourceDAO.getTaggedNewsitemsCount(tags, showBrokenDecisionService.shouldShowBroken)
  }

  def getTaggedNewsitems(tags: util.List[Tag], startIndex: Int, maxItems: Int): util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getTaggedNewsitems(tags, showBrokenDecisionService.shouldShowBroken, startIndex, maxItems)
  }

  def getTaggedNewsitems(tag: Tag, startIndex: Int, maxNewsitems: Int): util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getTaggedNewsitems(tag, showBrokenDecisionService.shouldShowBroken, startIndex, maxNewsitems)
  }

  def getTaggedWebsites(tag: Tag, maxItems: Int): util.List[FrontendResource] = {
    val tags: util.Set[Tag] = new util.HashSet[Tag]
    tags.add(tag)
    elasticSearchBackedResourceDAO.getTaggedWebsites(tags, showBrokenDecisionService.shouldShowBroken, maxItems)
  }

  def getPublisherTagCombinerNewsitems(publisher: Website, tag: Tag, maxNewsitems: Int): util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getPublisherTagCombinerNewsitems(publisher, tag, showBrokenDecisionService.shouldShowBroken, maxNewsitems)
  }

  def getPublisherTagCombinerNewsitems(publisherUrlWords: String, tagName: String, maxNewsitems: Int): util.List[FrontendResource] = {
    val publisher: Website = resourceDAO.getPublisherByUrlWords(publisherUrlWords)
    val tag: Tag = tagDAO.loadTagByName(tagName)
    if (publisher != null && tag != null) {
      this.getPublisherTagCombinerNewsitems(publisher, tag, maxNewsitems)
    } else {
      null
    }
  }

  def getRecentlyChangedWatchlistItems: util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getAllWatchlists(showBrokenDecisionService.shouldShowBroken)
  }

  def getFeedworthyTags: util.List[FrontendTag] = {
    val feedworthTags: util.List[FrontendTag] = Lists.newArrayList()
    import scala.collection.JavaConversions._
    for (tagContentCount <- relatedTagsService.getFeedworthyTags(showBrokenDecisionService.shouldShowBroken)) {
      feedworthTags.add(tagContentCount.getTag)
    }
    feedworthTags
  }

  def getDiscoveredFeeds: util.List[DiscoveredFeed] = {
    discoveredFeedsDAO.getAllNonCommentDiscoveredFeeds
  }

  def getOwnedBy(loggedInUser: User): util.List[FrontendResource] = {
    val owned: util.List[FrontendResource] = Lists.newArrayList()
    import scala.collection.JavaConversions._
    for (resource <- resourceDAO.getOwnedBy(loggedInUser, MAX_NEWSITEMS_TO_SHOW)) {
      owned.add(frontendResourceMapper.createFrontendResourceFrom(resource))
    }
    owned
  }

  def getTaggedBy(user: User): util.List[FrontendResource] = {
    elasticSearchBackedResourceDAO.getHandTaggingsForUser(user, showBrokenDecisionService.shouldShowBroken)
  }

  def getTagNamesStartingWith(q: String): util.List[String] = {
    tagDAO.getTagNamesStartingWith(q)
  }

  def getPublisherNamesByStartingLetters(q: String): util.List[String] = {
    resourceDAO.getPublisherNamesByStartingLetters(q)
  }

  def getOwnedByCount(loggedInUser: User): Int = {
    resourceDAO.getOwnedByUserCount(loggedInUser)
  }

  def getNewsPage(pathInfo: String): FrontendResource = {
    elasticSearchBackedResourceDAO.getNewspage(pathInfo, showBrokenDecisionService.shouldShowBroken)
  }

  def getFeaturedTags: util.List[Tag] = {
    tagDAO.getFeaturedTags
  }

}
