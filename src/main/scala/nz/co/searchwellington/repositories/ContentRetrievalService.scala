package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.helpers.CommonSizes
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

@Component class ContentRetrievalService @Autowired()(resourceDAO: HibernateResourceDAO,
                                                      keywordSearchService: KeywordSearchService,
                                                      showBrokenDecisionService: ShowBrokenDecisionService,
                                                      tagDAO: TagDAO, relatedTagsService: RelatedTagsService,
                                                      frontendResourceMapper: FrontendResourceMapper, elasticSearchIndexer: ElasticSearchIndexer,
                                                      mongoRepository: MongoRepository) extends ReasonableWaits with CommonSizes {

  private val log = Logger.getLogger(classOf[ContentRetrievalService])

  val ALL_ITEMS = 1000

  private val feeds = Some(Set("F"))
  private val newsitems = Some(Set("N"))
  private val watchlists = Some(Set("L"))
  private val websites = Some(Set("W"))

  private val allNewsitems = ResourceQuery(`type` = newsitems)

  def getAllPublishers(loggedInUser: Option[User]): Future[Seq[Website]] = {

    def getAllPublisherIds(loggedInUser: Option[User]): Future[Seq[(String, Long)]] = {
      val allPublishedTypes = ResourceQuery(`type` = Some(Set("N", "F", "L")))
      elasticSearchIndexer.getPublisherAggregationFor(allPublishedTypes, loggedInUser)
    }

    getAllPublisherIds(loggedInUser).flatMap { ids =>
      log.info("Got " + ids.size + " publisher ids")
      Future.sequence(ids.map { id =>
        mongoRepository.getResourceByObjectId(BSONObjectID(id._1)).map(ro => ro.map(_.asInstanceOf[Website]))
      }).map(_.flatten)
    }
  }

  def getPublisherNamesByStartingLetters(q: String, loggedInUser: Option[User]): Future[Seq[Website]] = {
    log.info("Get publishers starting with '" + q + "'")
    val isAdminUser = loggedInUser.exists(_.isAdmin) // TODO push down to same level as all publishers
    mongoRepository.getWebsitesByNamePrefix(q, isAdminUser)
  }

  def getTopLevelTags: Future[Seq[Tag]] = tagDAO.getTopLevelTags

  def getResourcesMatchingKeywordsNotTaggedByUser(keywords: Set[String], user: User, tag: Tag): Future[Seq[FrontendResource]] = {

    def getResourcesMatchingKeywordsNotTaggedByUser(keywords: Set[String], user: User, tag: Tag): Future[(Seq[BSONObjectID], Long)] = {
      // TODO exclude tagged by user
      val query = ResourceQuery(`type` = newsitems, q = Some(keywords.mkString(" ")))
      elasticSearchIndexer.getResources(query, loggedInUser = Some(user))
    }

    getResourcesMatchingKeywordsNotTaggedByUser(keywords, user, tag).flatMap(i => fetchByIds(i._1))
  }

  def getNewsitemsMatchingKeywords(keywords: String, startIndex: Int, maxNewsitems: Int, loggedInUser: Option[User], tag: Option[Tag] = None, publisher: Option[Website] = None): Future[(Seq[FrontendResource], Long)] = {
    val newsitemsByKeywords = ResourceQuery(`type` = newsitems, q = Some(keywords), publisher = publisher, tags = tag.map(t => Set(t)))
    toFrontendResourcesWithTotalCount(elasticSearchIndexer.getResources(newsitemsByKeywords, loggedInUser = loggedInUser))
  }


  def getTagWatchlist(tag: Tag, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val taggedWebsites = ResourceQuery(`type` = watchlists, tags = Some(Set(tag)))
    elasticSearchIndexer.getResources(taggedWebsites, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }


  def getTaggedFeeds(tag: Tag, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val taggedWebsites = ResourceQuery(`type` = feeds, tags = Some(Set(tag)))
    elasticSearchIndexer.getResources(taggedWebsites, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getGeocodedNewsitems(startIndex: Int, maxItems: Int, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    elasticSearchIndexer.getResources(geocodedNewsitems, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getGeocodedNewitemsCount(loggedInUser: Option[User]): Future[Long] = {
    elasticSearchIndexer.getResources(geocodedNewsitems, loggedInUser = loggedInUser).map(_._2)
  }

  def getNewsitemsNear(latLong: LatLong, radius: Double, startIndex: Int, maxNewsitems: Int, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val withPagination = nearbyNewsitems(latLong, radius).copy(startIndex = startIndex, maxItems = maxNewsitems)
    elasticSearchIndexer.getResources(withPagination, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getNewsitemsNearCount(latLong: LatLong, radius: Double, loggedInUser: Option[User]): Future[Long] = {
    elasticSearchIndexer.getResources(nearbyNewsitems(latLong, radius), loggedInUser = loggedInUser).map(i => i._2)
  }

  def getGeotaggedNewsitemsForTag(tag: Tag, maxItems: Int, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val geotaggedNewsitemsForTag = ResourceQuery(`type` = newsitems, geocoded = Some(true), tags = Some(Set(tag)), maxItems = ALL_ITEMS)
    elasticSearchIndexer.getResources(geotaggedNewsitemsForTag, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getLatestNewsitems(maxItems: Int = MAX_NEWSITEMS, page: Int = 1, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    elasticSearchIndexer.getResources(ResourceQuery(`type` = newsitems, maxItems = maxItems, startIndex = maxItems * (page - 1)), loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getNewsitemsForInterval(interval: Interval, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val newsitemsForMonth = ResourceQuery(`type` = newsitems, interval = Some(interval), maxItems = ALL_ITEMS)
    elasticSearchIndexer.getResources(newsitemsForMonth, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getNewsitemsForPublisherInterval(publisher: Website, interval: Interval, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val publisherNewsitemsForMonth = ResourceQuery(`type` = newsitems, publisher = Some(publisher), interval = Some(interval), maxItems = ALL_ITEMS)
    elasticSearchIndexer.getResources(publisherNewsitemsForMonth, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getPublishersForInterval(interval: Interval, loggedInUser: Option[User]): Future[Seq[(FrontendResource, Long)]] = {

    def getPublishersForInterval(interval: Interval, loggedInUser: Option[User]): Future[Seq[(String, Long)]] = {
      val newsitemsForInterval = ResourceQuery(`type` = newsitems, interval = Some(interval))
      elasticSearchIndexer.getPublisherAggregationFor(newsitemsForInterval, loggedInUser)
    }

    getPublishersForInterval(interval, loggedInUser).flatMap { meh =>
      Future.sequence(meh.map { t =>
        val bid = BSONObjectID.parse(t._1).get // TODO naked get
        val eventualMaybeResource = mongoRepository.getResourceByObjectId(bid)
        eventualMaybeResource.map { ro =>
          ro.map { r =>
            val eventualFrontennResource = frontendResourceMapper.createFrontendResourceFrom(r)
            (Await.result(eventualFrontennResource, TenSeconds), t._2)
          }
        }
      }).map(_.flatten)
    }
  }

  def getLatestWebsites(maxItems: Int, page: Int = 1, loggedInUser: Option[User]): Future[(Seq[FrontendResource], Long)] = {
    val latestWebsites = ResourceQuery(`type` = websites, maxItems = maxItems, startIndex = maxItems * (page - 1))
    elasticSearchIndexer.getResources(latestWebsites, loggedInUser = loggedInUser).flatMap(buildFrontendResourcesFor)
  }

  def getOwnedBy(user: User, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    elasticSearchIndexer.getResources(
      ResourceQuery(
        owner = Some(user._id),
        maxItems = MAX_NEWSITEMS
      ),
      loggedInUser = loggedInUser
    ).flatMap(i => fetchByIds(i._1))
  }

  def getTaggedBy(user: User, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    elasticSearchIndexer.getResources(
      ResourceQuery(
        taggingUser = Some(user._id),
        maxItems = MAX_NEWSITEMS
      ),
      loggedInUser = loggedInUser
    ).flatMap(i => fetchByIds(i._1))
  }

  def getKeywordSearchFacets(keywords: String): Seq[TagContentCount] = {
    relatedTagsService.getKeywordSearchFacets(keywords, null) // TODO This is abit odd - it's the only facet one which comes through here.
  }

  def getWebsitesMatchingKeywords(keywords: String, tag: Tag, startIndex: Int, maxItems: Int, loggedInUser: Option[User]): Seq[FrontendResource] = {
    keywordSearchService.getWebsitesMatchingKeywords(keywords, showBrokenDecisionService.shouldShowBroken(loggedInUser), tag, startIndex, maxItems)
  }

  def getArchiveMonths(loggedInUser: Option[User]): Future[Seq[ArchiveLink]] = {
    def getArchiveMonths(loggedInUser: Option[User]): Future[Seq[(Interval, Long)]] = {
      elasticSearchIndexer.createdMonthAggregationFor(allNewsitems, loggedInUser)
    }

    getArchiveMonths(loggedInUser).map(archiveLinksFromIntervals)
  }

  def getPublisherArchiveMonths(publisher: Website, loggedInUser: Option[User]): Future[Seq[ArchiveLink]] = {
    def getPublisherArchiveMonths(publisher: Website, loggedInUser: Option[User]): Future[Seq[(Interval, Long)]] = {
      val publisherNewsitems = ResourceQuery(`type` = newsitems, publisher = Some(publisher))
      elasticSearchIndexer.createdMonthAggregationFor(publisherNewsitems, loggedInUser)
    }

    getPublisherArchiveMonths(publisher, loggedInUser).map(archiveLinksFromIntervals)
  }

  def getArchiveCounts(loggedInUser: Option[User]): Future[Map[String, Long]] = elasticSearchIndexer.getArchiveCounts(loggedInUser)

  def getFeeds(acceptancePolicy: Option[FeedAcceptancePolicy] = None, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val allFeeds = ResourceQuery(`type` = feeds, maxItems = ALL_ITEMS)

    val withAcceptancePolicy = acceptancePolicy.map { a =>
      allFeeds.copy(feedAcceptancePolicy = Some(a))
    }.getOrElse(allFeeds)

    elasticSearchIndexer.getResources(withAcceptancePolicy, elasticSearchIndexer.byTitleAscending, loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getAllFeedsOrderedByLatestItemDate(loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val allFeeds = ResourceQuery(`type` = feeds)
    elasticSearchIndexer.getResources(allFeeds, elasticSearchIndexer.byFeedLatestFeedItemDate, loggedInUser = loggedInUser).
      flatMap(i => fetchByIds(i._1)) // TODO order
  }

  def getTaggedNewsitems(tag: Tag, startIndex: Int = 0, maxItems: Int = MAX_NEWSITEMS, loggedInUser: Option[User]): Future[(Seq[FrontendResource], Long)] = {
    getTaggedNewsitems(tags = Set(tag), startIndex = startIndex, maxItems = maxItems, loggedInUser)
  }

  def getTaggedNewsitems(tags: Set[Tag], startIndex: Int, maxItems: Int, loggedInUser: Option[User]): Future[(Seq[FrontendResource], Long)] = {
    val taggedNewsitems = ResourceQuery(`type` = newsitems, tags = Some(tags), startIndex = startIndex, maxItems = maxItems)
    elasticSearchIndexer.getResources(taggedNewsitems, loggedInUser = loggedInUser).flatMap(buildFrontendResourcesFor)
  }

  def getWatchlistItems(loggedInUser: Option[User], page: Int): Future[(Seq[FrontendResource], Long)] = {
    val watchListItems = ResourceQuery(`type` = watchlists, maxItems = MAX_NEWSITEMS, startIndex = MAX_NEWSITEMS * (page - 1))
    elasticSearchIndexer.getResources(watchListItems, loggedInUser = loggedInUser).flatMap(buildFrontendResourcesFor)
  }

  def getTaggedWebsites(tag: Tag, maxItems: Int, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val taggedWebsites = ResourceQuery(`type` = websites, tags = Some(Set(tag)), maxItems = maxItems)
    elasticSearchIndexer.getResources(taggedWebsites, elasticSearchIndexer.byTitleAscending, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getPublisherNewsitems(publisher: Website, maxItems: Int, startIndex: Int, loggedInUser: Option[User]): Future[(Seq[FrontendResource], Long)] = {
    val publisherNewsitems = ResourceQuery(`type` = newsitems, publisher = Some(publisher), startIndex = startIndex, maxItems = maxItems)
    elasticSearchIndexer.getResources(publisherNewsitems, loggedInUser = loggedInUser).flatMap(buildFrontendResourcesFor)
  }

  def getPublisherFeeds(publisher: Website, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val publisherFeeds = ResourceQuery(`type` = feeds, publisher = Some(publisher))
    elasticSearchIndexer.getResources(publisherFeeds, elasticSearchIndexer.byTitleAscending, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getPublisherWatchlist(publisher: Website, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val publisherWatchlist = ResourceQuery(`type` = watchlists, publisher = Some(publisher))
    elasticSearchIndexer.getResources(publisherWatchlist, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getPublisherTagCombinerNewsitems(publisher: Website, tag: Tag, maxNewsitems: Int, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val publisherTagCombiner = ResourceQuery(`type` = newsitems, publisher = Some(publisher), tags = Some(Set(tag)))
    elasticSearchIndexer.getResources(publisherTagCombiner, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getFeedworthyTags(loggedInUser: Option[User]): Seq[Tag] = {
    var feedworthTags: Seq[Tag] = Seq()
    import scala.collection.JavaConversions._
    for (tagContentCount <- relatedTagsService.getFeedworthyTags(showBrokenDecisionService.shouldShowBroken(loggedInUser))) {
      feedworthTags.add(tagContentCount.getTag)
    }
    feedworthTags
  }

  def getDiscoveredFeeds: Future[Seq[DiscoveredFeed]] = {
    mongoRepository.getAllDiscoveredFeeds()
  }

  def getTagNamesStartingWith(q: String, loggedInUser: Option[User]): Future[Seq[String]] = tagDAO.getTagNamesStartingWith(q)

  def getOwnedByCount(loggedInUser: User): Int = {
    resourceDAO.getOwnedByUserCount(loggedInUser)
  }

  def getNewsPage(pathInfo: String): Option[FrontendResource] = {
    //elasticSearchBackedResourceDAO.getNewspage(pathInfo, showBrokenDecisionService.shouldShowBroken)
    None
  }

  def getFeaturedTags: Future[Seq[Tag]] = tagDAO.getFeaturedTags

  private def buildFrontendResourcesFor(i: (Seq[BSONObjectID], Long)): Future[(Seq[FrontendResource], Long)] = {
    fetchByIds(i._1).map { rs =>
      (rs, i._2)
    }
  }

  private val geocodedNewsitems = ResourceQuery(`type` = newsitems, geocoded = Some(true))

  private def nearbyNewsitems(latLong: LatLong, radius: Double) = ResourceQuery(`type` = newsitems, circle = Some(Circle(latLong, radius)))

  private def toFrontendResourcesWithTotalCount(elasticSearchResults: Future[(Seq[BSONObjectID], Long)]): Future[(Seq[FrontendResource], Long)] = {
    elasticSearchResults.flatMap { i =>
      fetchByIds(i._1).map { rs =>
        (rs, i._2)
      }
    }
  }

  private def fetchByIds(ids: Seq[BSONObjectID]): Future[Seq[FrontendResource]] = {
    val eventualResources = Future.sequence {
      ids.map { id =>
        mongoRepository.getResourceByObjectId(id)
      }
    }.map(_.flatten)

    eventualResources.flatMap { rs =>
      Future.sequence(rs.map(r => frontendResourceMapper.createFrontendResourceFrom(r)))
    }
  }

  private def archiveLinksFromIntervals(intervals: Seq[(Interval, Long)]) = {
    intervals.map { i =>
      ArchiveLink(i._1, i._2)
    }
  }

}
