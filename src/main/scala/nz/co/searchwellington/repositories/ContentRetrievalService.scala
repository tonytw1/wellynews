package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.helpers.CommonSizes
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.elasticsearch._
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.joda.time.{DateTime, Interval}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID
import uk.co.eelpieconsulting.common.geo.model.LatLong

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class ContentRetrievalService @Autowired()(tagDAO: TagDAO,
                                                      frontendResourceMapper: FrontendResourceMapper,
                                                      elasticSearchIndexer: ElasticSearchIndexer,
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
        mongoRepository.getResourceByObjectId(BSONObjectID.parse(id._1).get).map(ro => ro.map(_.asInstanceOf[Website]))
      }).map(_.flatten)
    }
  }

  def getPublisherNamesByStartingLetters(q: String, loggedInUser: Option[User]): Future[Seq[Website]] = {
    log.info("Get publishers starting with '" + q + "'")
    val isAdminUser = loggedInUser.exists(_.isAdmin) // TODO push down to same level as all publishers
    mongoRepository.getWebsitesByNamePrefix(q, isAdminUser)
  }

  def getTopLevelTags: Future[Seq[Tag]] = tagDAO.getTopLevelTags

  def getNewsitemsMatchingKeywordsNotTaggedByUser(keywords: Set[String], user: User, tag: Tag, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {

    def getNewsitemIDsMatchingKeywords(keywords: Set[String], user: User): Future[(Seq[BSONObjectID], Long)] = {
      val query = ResourceQuery(`type` = newsitems, q = Some(keywords.mkString(" ")))
      elasticSearchIndexer.getResources(query, loggedInUser = Some(user))
    }

    getNewsitemIDsMatchingKeywords(keywords, user).flatMap(i => fetchResourcesByIds(i._1)).map { resources =>
      resources.filter { r =>
        val hasExistingTagging = r.resource_tags.exists{ tagging =>
          val bool = tagging.user_id == user._id && tagging.tag_id == tag._id
          log.info("Existing tagging: " + tagging.tag_id + " / " + tag._id  + ": " + bool)
          bool
        }
        !hasExistingTagging
      }
    }.flatMap{ rs =>
      Future.sequence(rs.map{ r => frontendResourceMapper.createFrontendResourceFrom(r, loggedInUser)})
    }
  }

  def getNewsitemsMatchingKeywords(keywords: String, startIndex: Int, maxNewsitems: Int, loggedInUser: Option[User], tag: Option[Tag] = None, publisher: Option[Website] = None): Future[(Seq[FrontendResource], Long)] = {
    val newsitemsByKeywords = ResourceQuery(`type` = newsitems, q = Some(keywords), publisher = publisher, tags = tag.map(t => Set(t)))
    toFrontendResourcesWithTotalCount(elasticSearchIndexer.getResources(newsitemsByKeywords, loggedInUser = loggedInUser), loggedInUser)
  }

  def getNewsitemKeywordSearchRelatedTags(keywords: String, loggedInUser: Option[User]): Future[Seq[TagContentCount]] = {
    val newsitemsByKeywords = ResourceQuery(`type` = newsitems, q = Some(keywords), publisher = None, tags = None)

    val tagAggregation = elasticSearchIndexer.getAggregationFor(newsitemsByKeywords, elasticSearchIndexer.Tags, loggedInUser)

    // TODO duplication
    def toTagContentCount(facet: (String, Long)): Future[Option[TagContentCount]] = {
      mongoRepository.getTagByObjectId(BSONObjectID.parse(facet._1).get).map { to =>
        to.map { tag =>
          TagContentCount(tag, facet._2)
        }
      }
    }

    tagAggregation.flatMap { ts =>
      Future.sequence(ts.map(toTagContentCount)).map(_.flatten)
    }
  }
  def getNewsitemKeywordSearchRelatedPublishers(keywords: String, loggedInUser: Option[User]): Future[Seq[PublisherContentCount]] = {
    val newsitemsByKeywords = ResourceQuery(`type` = newsitems, q = Some(keywords), publisher = None, tags = None)

    val publisherAggregation = elasticSearchIndexer.getAggregationFor(newsitemsByKeywords, elasticSearchIndexer.Publisher, loggedInUser)

    // TODO duplication
    def toPublisherContentCount(facet: (String, Long)): Future[Option[PublisherContentCount]] = {
      mongoRepository.getResourceByObjectId(BSONObjectID.parse(facet._1).get).map { to =>
        to.flatMap { resource =>
          resource match {
            case publisher: Website =>
              Some(PublisherContentCount(publisher, facet._2))
            case _ =>
              None
          }
        }
      }
    }

    publisherAggregation.flatMap { ts =>
      Future.sequence(ts.map(toPublisherContentCount)).map(_.flatten)
    }
  }

  def getTagWatchlist(tag: Tag, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val taggedWebsites = ResourceQuery(`type` = watchlists, tags = Some(Set(tag)))
    elasticSearchIndexer.getResources(taggedWebsites, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1, loggedInUser))
  }


  def getTaggedFeeds(tag: Tag, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val taggedWebsites = ResourceQuery(`type` = feeds, tags = Some(Set(tag)))
    elasticSearchIndexer.getResources(taggedWebsites, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1, loggedInUser))
  }

  def getGeocodedNewsitems(startIndex: Int, maxItems: Int, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    elasticSearchIndexer.getResources(geocodedNewsitems, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1, loggedInUser))
  }

  def getGeocodedNewitemsCount(loggedInUser: Option[User]): Future[Long] = {
    elasticSearchIndexer.getResources(geocodedNewsitems, loggedInUser = loggedInUser).map(_._2)
  }

  def getNewsitemsNear(latLong: LatLong, radius: Double, startIndex: Int, maxNewsitems: Int, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val withPagination = nearbyNewsitems(latLong, radius).copy(startIndex = startIndex, maxItems = maxNewsitems)
    elasticSearchIndexer.getResources(withPagination, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1, loggedInUser))
  }

  def getNewsitemsNearCount(latLong: LatLong, radius: Double, loggedInUser: Option[User]): Future[Long] = {
    elasticSearchIndexer.getResources(nearbyNewsitems(latLong, radius), loggedInUser = loggedInUser).map(i => i._2)
  }

  def getGeotaggedNewsitemsForTag(tag: Tag, maxItems: Int, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val geotaggedNewsitemsForTag = ResourceQuery(`type` = newsitems, geocoded = Some(true), tags = Some(Set(tag)), maxItems = maxItems) // TODO page size
    elasticSearchIndexer.getResources(geotaggedNewsitemsForTag, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1, loggedInUser))
  }

  def getLatestNewsitems(maxItems: Int = MAX_NEWSITEMS, page: Int = 1, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    elasticSearchIndexer.getResources(ResourceQuery(`type` = newsitems,
      before = Some(DateTime.now().plusWeeks(1)),
      maxItems = maxItems,
      startIndex = maxItems * (page - 1)
    ), loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1, loggedInUser))
  }

  def getNewsitemsForInterval(interval: Interval, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val newsitemsForMonth = ResourceQuery(`type` = newsitems, interval = Some(interval), maxItems = ALL_ITEMS)
    elasticSearchIndexer.getResources(newsitemsForMonth, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1, loggedInUser))
  }

  def getNewsitemsForPublisherInterval(publisher: Website, interval: Interval, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val publisherNewsitemsForMonth = ResourceQuery(`type` = newsitems, publisher = Some(publisher), interval = Some(interval), maxItems = ALL_ITEMS)
    elasticSearchIndexer.getResources(publisherNewsitemsForMonth, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1, loggedInUser))
  }

  def getPublishersForInterval(interval: Interval, loggedInUser: Option[User]): Future[Seq[(FrontendResource, Long)]] = {

    def getPublisherIdsAndCountsForInterval(interval: Interval, loggedInUser: Option[User]): Future[Seq[(String, Long)]] = {
      val newsitemsForInterval = ResourceQuery(`type` = newsitems, interval = Some(interval))
      elasticSearchIndexer.getPublisherAggregationFor(newsitemsForInterval, loggedInUser)
    }

    def toFrontendResourceWithCount(t: (String, Long)): Future[Option[(FrontendResource, Long)]] = {
      BSONObjectID.parse(t._1).map { bid =>
        mongoRepository.getResourceByObjectId(bid).flatMap { maybeResource =>
          maybeResource.map { resource: Resource =>
            val eventualResource = frontendResourceMapper.createFrontendResourceFrom(resource)
            eventualResource.map { frontendResource =>
              Some((frontendResource, t._2))
            }
          }.getOrElse(Future.successful(None))
        }
      }.getOrElse {
        Future.successful(None)
      }
    }

    getPublisherIdsAndCountsForInterval(interval, loggedInUser).flatMap { stringLongPairs =>
      val eventualMaybeTuples: Seq[Future[Option[(FrontendResource, Long)]]] = stringLongPairs.map(toFrontendResourceWithCount)
      Future.sequence(eventualMaybeTuples).map(_.flatten)
    }
  }

  def getLatestWebsites(maxItems: Int, page: Int = 1, loggedInUser: Option[User]): Future[(Seq[FrontendResource], Long)] = {
    val latestWebsites = ResourceQuery(`type` = websites, maxItems = maxItems, startIndex = maxItems * (page - 1))
    elasticSearchIndexer.getResources(latestWebsites, loggedInUser = loggedInUser).flatMap(r => buildFrontendResourcesFor(r, loggedInUser))
  }

  def getAcceptedNewsitems(maxItems: Int, page: Int = 1, loggedInUser: Option[User], held: Option[Boolean] = None): Future[(Seq[FrontendResource], Long)] = {
    val acceptNewsitems = ResourceQuery(`type` = newsitems, maxItems = maxItems, startIndex = maxItems * (page - 1)) // TODO needs not null clause
    elasticSearchIndexer.getResources(acceptNewsitems, elasticSearchIndexer.byAcceptedDate, loggedInUser = loggedInUser).flatMap(r => buildFrontendResourcesFor(r, loggedInUser))
  }

  def getOwnedBy(user: User, loggedInUser: Option[User], maxItems: Int): Future[(Seq[FrontendResource], Long)] = {
    val ownedBy = ResourceQuery(
      owner = Some(user._id),
      maxItems = maxItems
    )
    elasticSearchIndexer.getResources(ownedBy, elasticSearchIndexer.byDateDescending, loggedInUser = loggedInUser).flatMap(r => buildFrontendResourcesFor(r, loggedInUser))
  }

  def getTaggedBy(user: User, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val taggedByUser = ResourceQuery(
      taggingUser = Some(user._id),
      maxItems = MAX_NEWSITEMS
    )
    elasticSearchIndexer.getResources(
      taggedByUser,
      loggedInUser = loggedInUser
    ).flatMap(i => fetchByIds(i._1, loggedInUser))
  }

  def getWebsitesMatchingKeywords(keywords: String, tag: Option[Tag], startIndex: Int, maxItems: Int, loggedInUser: Option[User]): Future[(Seq[FrontendResource], Long)] = {
    val websitesByKeyword = ResourceQuery(`type` = websites,
      q = Some(keywords),
      tags = tag.map(t => Set(t)),
      startIndex = startIndex,
      maxItems = maxItems
    )
    toFrontendResourcesWithTotalCount(elasticSearchIndexer.getResources(websitesByKeyword, loggedInUser = loggedInUser), loggedInUser)
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

    elasticSearchIndexer.getResources(withAcceptancePolicy, elasticSearchIndexer.byTitleAscending, loggedInUser).flatMap(i => fetchByIds(i._1, loggedInUser))
  }

  def getSuggestOnlyFeeds(loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val suggestOnlyFeeds = ResourceQuery(`type` = feeds, maxItems = ALL_ITEMS, feedAcceptancePolicy = Some(FeedAcceptancePolicy.SUGGEST))
    elasticSearchIndexer.getResources(suggestOnlyFeeds, elasticSearchIndexer.byFeedLatestFeedItemDate, loggedInUser).flatMap(i => fetchByIds(i._1, loggedInUser))
  }

  def getAllFeedsOrderedByLatestItemDate(loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val allFeeds = ResourceQuery(`type` = feeds)
    elasticSearchIndexer.getResources(allFeeds, elasticSearchIndexer.byFeedLatestFeedItemDate, loggedInUser = loggedInUser).
      flatMap(i => fetchByIds(i._1, loggedInUser))
  }

  def getTaggedNewsitems(tag: Tag, startIndex: Int = 0, maxItems: Int = MAX_NEWSITEMS, loggedInUser: Option[User]): Future[(Seq[FrontendResource], Long)] = {
    getTaggedNewsitems(tags = Set(tag), startIndex = startIndex, maxItems = maxItems, loggedInUser)
  }

  def getTaggedNewsitems(tags: Set[Tag], startIndex: Int, maxItems: Int, loggedInUser: Option[User]): Future[(Seq[FrontendResource], Long)] = {
    val taggedNewsitems = ResourceQuery(`type` = newsitems, tags = Some(tags), startIndex = startIndex, maxItems = maxItems)
    elasticSearchIndexer.getResources(taggedNewsitems, loggedInUser = loggedInUser).flatMap(r => buildFrontendResourcesFor(r, loggedInUser))
  }

  def getWatchlistItems(loggedInUser: Option[User], page: Int): Future[(Seq[FrontendResource], Long)] = {
    val watchListItems = ResourceQuery(`type` = watchlists, maxItems = MAX_NEWSITEMS, startIndex = MAX_NEWSITEMS * (page - 1))
    elasticSearchIndexer.getResources(watchListItems, elasticSearchIndexer.byLastChangedDescending, loggedInUser = loggedInUser).flatMap(r => buildFrontendResourcesFor(r, loggedInUser))
  }

  def getTaggedWebsites(tag: Tag, maxItems: Int, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val taggedWebsites = ResourceQuery(`type` = websites, tags = Some(Set(tag)), maxItems = maxItems)
    elasticSearchIndexer.getResources(taggedWebsites, elasticSearchIndexer.byTitleAscending, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1, loggedInUser))
  }

  def getPublisherNewsitems(publisher: Website, maxItems: Int, startIndex: Int, loggedInUser: Option[User]): Future[(Seq[FrontendResource], Long)] = {
    val publisherNewsitems = ResourceQuery(`type` = newsitems, publisher = Some(publisher), startIndex = startIndex, maxItems = maxItems)
    elasticSearchIndexer.getResources(publisherNewsitems, loggedInUser = loggedInUser).flatMap(r => buildFrontendResourcesFor(r, loggedInUser))
  }

  def getPublisherFeeds(publisher: Website, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val publisherFeeds = ResourceQuery(`type` = feeds, publisher = Some(publisher))
    elasticSearchIndexer.getResources(publisherFeeds, elasticSearchIndexer.byTitleAscending, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1, loggedInUser))
  }

  def getPublisherWatchlist(publisher: Website, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val publisherWatchlist = ResourceQuery(`type` = watchlists, publisher = Some(publisher))
    elasticSearchIndexer.getResources(publisherWatchlist, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1, loggedInUser))
  }

  def getPublisherTagCombinerNewsitems(publisher: Website, tag: Tag, maxNewsitems: Int, loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    val publisherTagCombiner = ResourceQuery(`type` = newsitems, publisher = Some(publisher), tags = Some(Set(tag)))
    elasticSearchIndexer.getResources(publisherTagCombiner, loggedInUser = loggedInUser).flatMap(i => fetchByIds(i._1, loggedInUser))
  }

  def getDiscoveredFeeds(maxNumber: Int): Future[Seq[DiscoveredFeed]] = {
    mongoRepository.getDiscoveredFeeds(maxNumber)
  }

  def getTagNamesStartingWith(q: String, loggedInUser: Option[User]): Future[Seq[String]] = tagDAO.getTagNamesStartingWith(q)

  def getFeaturedTags: Future[Seq[Tag]] = tagDAO.getFeaturedTags

  def getWebsitesByHostname(hostname: String, loggedInUser: Option[User]): Future[Seq[Resource]] = {
    elasticSearchIndexer.getResources(ResourceQuery(hostname = Some(hostname), `type` = Some(Set("W"))), loggedInUser = loggedInUser).flatMap(i => fetchResourcesByIds(i._1))
  }

  private def buildFrontendResourcesFor(i: (Seq[BSONObjectID], Long), loggedInUser: Option[User]): Future[(Seq[FrontendResource], Long)] = {
    fetchByIds(i._1 ,loggedInUser).map { rs =>
      (rs, i._2)
    }
  }

  private val geocodedNewsitems = ResourceQuery(`type` = newsitems, geocoded = Some(true))

  private def nearbyNewsitems(latLong: LatLong, radius: Double) = ResourceQuery(`type` = newsitems, circle = Some(Circle(latLong, radius)))

  private def toFrontendResourcesWithTotalCount(elasticSearchResults: Future[(Seq[BSONObjectID], Long)], loggedInUser: Option[User]): Future[(Seq[FrontendResource], Long)] = {
    elasticSearchResults.flatMap { i =>
      fetchByIds(i._1, loggedInUser).map { rs =>
        (rs, i._2)
      }
    }
  }

  private def fetchByIds(ids: Seq[BSONObjectID], loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    fetchResourcesByIds(ids).flatMap { rs =>
      Future.sequence(rs.map(r => frontendResourceMapper.createFrontendResourceFrom(r, loggedInUser)))
    }
  }

  private def fetchResourcesByIds(ids: Seq[BSONObjectID]): Future[Seq[Resource]] = {
    Future.sequence {
      ids.map { id =>
        mongoRepository.getResourceByObjectId(id)
      }
    }.map(_.flatten)
  }

  private def archiveLinksFromIntervals(intervals: Seq[(Interval, Long)]): Seq[ArchiveLink] = {
    intervals.map { i =>
      ArchiveLink(i._1, i._2)
    }
  }

}
