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

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

@Component class ContentRetrievalService @Autowired()(resourceDAO: HibernateResourceDAO,
                                                      keywordSearchService: KeywordSearchService,
                                                      showBrokenDecisionService: ShowBrokenDecisionService,
                                                      tagDAO: TagDAO, relatedTagsService: RelatedTagsService,
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

  def getTopLevelTags: Future[Seq[Tag]] = tagDAO.getTopLevelTags

  def getResourcesMatchingKeywordsNotTaggedByUser(keywords: Set[String], user: User, tag: Tag): Future[Seq[FrontendResource]] = {
    elasticSearchIndexer.getResourcesMatchingKeywordsNotTaggedByUser(keywords, user, tag).flatMap(i => fetchByIds(i._1))
  }

  def getNewsitemsMatchingKeywords(keywords: String, startIndex: Int, maxNewsitems: Int, loggedInUser: Option[User]): Future[(Seq[FrontendResource], Long)] = {
    val newsitemsByKeywords = ResourceQuery(`type` = Some("N"), q = Some(keywords))
    toFrontendResourcesWithTotalCount(elasticSearchIndexer.getResources(newsitemsByKeywords, loggedInUser = loggedInUser))
  }

  def getTagNewsitemsMatchingKeywords(keywords: String, tag: Tag, startIndex: Int, maxItems: Int, loggedInUser: Option[User]): Future[(Seq[FrontendResource], Long)] = {
    val taggedNewsitemsByKeywords = ResourceQuery(`type` = Some("N"), q = Some(keywords), tags = Some(Set(tag)))
    toFrontendResourcesWithTotalCount(elasticSearchIndexer.getResources(taggedNewsitemsByKeywords, loggedInUser = loggedInUser))
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
    val geotaggedNewsitemsForTag = ResourceQuery(`type` = Some("N"), geocoded = Some(true), tags = Some(Set(tag)), maxItems = ALL_ITEMS)
    elasticSearchIndexer.getResources(geotaggedNewsitemsForTag, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getLatestNewsitems(maxItems: Int = MAX_NEWSITEMS_TO_SHOW, page: Int = 1, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    elasticSearchIndexer.getResources(ResourceQuery(`type` = Some("N"), maxItems = maxItems, startIndex = maxItems * (page - 1)), loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getNewsitemsForInterval(interval: Interval, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val newsitemsForMonth = ResourceQuery(`type` = Some("N"), interval = Some(interval), maxItems = ALL_ITEMS)
    elasticSearchIndexer.getResources(newsitemsForMonth, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getNewsitemsForPublisherInterval(publisher: Website, interval: Interval, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val publisherNewsitemsForMonth = ResourceQuery(`type` = Some("N"), publisher = Some(publisher), interval = Some(interval), maxItems = ALL_ITEMS)
    elasticSearchIndexer.getResources(publisherNewsitemsForMonth, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getPublishersForInterval(interval: Interval, loggedInUser: Option[User]): Future[Seq[(FrontendResource, Long)]] = {
    elasticSearchIndexer.getPublishersForInterval(interval, loggedInUser).flatMap { meh =>
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

  def getLatestWebsites(maxItems: Int, page: Int = 1, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    elasticSearchIndexer.getResources(ResourceQuery(`type` = Some("W"), maxItems = maxItems, startIndex = maxItems * (page - 1)), loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getOwnedBy(user: User, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    elasticSearchIndexer.getResources(
      ResourceQuery(
        owner = Some(user._id),
        maxItems = MAX_NEWSITEMS_TO_SHOW
      ),
      loggedInUser = loggedInUser
    ).flatMap(i => fetchByIds(i._1))
  }

  def getTaggedBy(user: User, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    elasticSearchIndexer.getResources(
      ResourceQuery(
        taggingUser = Some(user._id),
        maxItems = MAX_NEWSITEMS_TO_SHOW
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

  def getArchiveMonths(loggedInUser: Option[User]): Future[Seq[ArchiveLink]] = elasticSearchIndexer.getArchiveMonths(loggedInUser)

  def getPublisherArchiveMonths(publisher: Website, loggedInUser: Option[User]): Future[Seq[ArchiveLink]] = elasticSearchIndexer.getPublisherArchiveMonths(publisher, loggedInUser)

  def getArchiveCounts(loggedInUser: Option[User]): Future[Map[String, Long]] = elasticSearchIndexer.getArchiveCounts(loggedInUser)

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

  def getTaggedNewsitems(tag: Tag, startIndex: Int = 0, maxItems: Int = MAX_NEWSITEMS_TO_SHOW, loggedInUser: Option[User]): Future[(Seq[FrontendResource], Long)] = {
    getTaggedNewsitems(tags = Set(tag), startIndex = startIndex, maxItems = maxItems, loggedInUser)
  }

  def getTaggedNewsitems(tags: Set[Tag], startIndex: Int, maxItems: Int, loggedInUser: Option[User]): Future[(Seq[FrontendResource], Long)] = {
    val taggedNewsitems = ResourceQuery(`type` = Some("N"), tags = Some(tags), startIndex = startIndex, maxItems = maxItems)
    elasticSearchIndexer.getResources(taggedNewsitems, loggedInUser = loggedInUser).flatMap(buildFrontendResourcesFor)
  }

  def getTaggedWebsites(tag: Tag, maxItems: Int, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val taggedWebsites = ResourceQuery(`type` = Some("W"), tags = Some(Set(tag)), maxItems = maxItems)
    elasticSearchIndexer.getResources(taggedWebsites, elasticSearchIndexer.byTitleAscending, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getPublisherNewsitems(publisher: Website, maxItems: Int, startIndex: Int, loggedInUser: Option[User]): Future[(Seq[FrontendResource], Long)] = {
    val publisherNewsitems = ResourceQuery(`type` = Some("N"), publisher = Some(publisher), startIndex = startIndex, maxItems = maxItems)
    elasticSearchIndexer.getResources(publisherNewsitems, loggedInUser = loggedInUser).flatMap(buildFrontendResourcesFor)
  }

  def getPublisherFeeds(publisher: Website, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val publisherFeeds = ResourceQuery(`type` = Some("F"), publisher = Some(publisher))
    elasticSearchIndexer.getResources(publisherFeeds, elasticSearchIndexer.byTitleAscending, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getAllWatchlists(loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val allWatchlists = ResourceQuery(`type` = Some("L"))
    elasticSearchIndexer.getResources(allWatchlists, elasticSearchIndexer.byTitleAscending, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getPublisherWatchlist(publisher: Website, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val publisherWatchlist = ResourceQuery(`type` = Some("L"), publisher = Some(publisher))
    elasticSearchIndexer.getResources(publisherWatchlist, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1))
  }

  def getPublisherTagCombinerNewsitems(publisher: Website, tag: Tag, maxNewsitems: Int, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val publisherTagCombiner = ResourceQuery(`type` = Some("N"), publisher = Some(publisher), tags = Some(Set(tag)))
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

  def getTagNamesStartingWith(q: String): Future[Seq[String]] = tagDAO.getTagNamesStartingWith(q)

  def getPublisherNamesByStartingLetters(q: String): Future[Seq[String]] = {
    log.info("Get publishers starting with '" + q + "'")
    mongoRepository.getWebsiteByNamePrefix(q).map { ps =>
      ps.map { p =>
        p.title.getOrElse("")
      }
    }
  }

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

  private val geocodedNewsitems = ResourceQuery(`type` = Some("N"), geocoded = Some(true))

  private def nearbyNewsitems(latLong: LatLong, radius: Double) = ResourceQuery(`type` = Some("N"), circle = Some(Circle(latLong, radius)))

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

}
