package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.controllers.ShowBrokenDecisionService
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, Newsitem, Resource}
import nz.co.searchwellington.repositories.HandTaggingDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import org.joda.time.{DateTime, Interval}
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test
import org.mockito.Mockito.mock

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ElasticSearchIT {

  private val showBrokenDecisionService = mock(classOf[ShowBrokenDecisionService])

  val mongoRepository = new MongoRepository("mongodb://localhost:27017/searchwellington")
  val elasticSearchIndexer = new ElasticSearchIndexer(showBrokenDecisionService, "10.0.45.11", 32400)
  val taggingReturnsOfficerService = new TaggingReturnsOfficerService(new HandTaggingDAO(mongoRepository), mongoRepository)

  val rebuild = new ElasticSearchIndexRebuildService(mongoRepository, elasticSearchIndexer, taggingReturnsOfficerService)

  private val TenSeconds = Duration(10, SECONDS)

  @Test
  def canCreateIndexes: Unit = {
    elasticSearchIndexer.createIndexes()
  }

  @Test
  def canIndexResources {
    rebuild.buildIndex(false)
  }

  @Test
  def canFilterByType {
    val newsitems = Await.result(elasticSearchIndexer.getResources(ResourceQuery(`type` = Some("N"))), TenSeconds)
    assertTrue(newsitems._1.nonEmpty)
    assertTrue(newsitems._1.forall(i => Await.result(mongoRepository.getResourceByObjectId(i), TenSeconds).get.`type` == "N"))

    val websites = Await.result(elasticSearchIndexer.getResources(ResourceQuery(`type` = Some("W"))), TenSeconds)
    assertTrue(websites._1.nonEmpty)
    assertTrue(websites._1.forall(i => Await.result(mongoRepository.getResourceByObjectId(i), TenSeconds).get.`type` == "W"))
  }

  @Test
  def canFilterByFeedAcceptancePolicy {
    val autoAcceptFeeds = Await.result(elasticSearchIndexer.getResources(ResourceQuery(`type` = Some("F"),
      feedAcceptancePolicy = Some(FeedAcceptancePolicy.ACCEPT))), TenSeconds)

    assertTrue(autoAcceptFeeds._1.nonEmpty)

    assertTrue(autoAcceptFeeds._1.forall(i => Await.result(mongoRepository.getResourceByObjectId(i), TenSeconds).get.
      asInstanceOf[Feed].acceptance == FeedAcceptancePolicy.ACCEPT))
  }

  @Test
  def canFilterByTag {
    val tag = Await.result(mongoRepository.getTagByUrlWords("arovalley"), TenSeconds).get
    val withTag = ResourceQuery(tags = Some(Set(tag)))

    val taggedNewsitemsQuery = withTag.copy(`type` = Some("N"))
    val taggedNewsitems = queryForResources(taggedNewsitemsQuery)
    assertTrue(taggedNewsitems.nonEmpty)
    assertTrue(taggedNewsitems.forall(i => i.`type` == "N"))
    assertTrue(taggedNewsitems.forall(i => i.resource_tags.exists(t => t.tag_id == tag._id)))

    val taggedWebsitesQuery = withTag.copy(`type` = Some("W"))
    val taggedWebsites = queryForResources(taggedWebsitesQuery)
    assertTrue(taggedWebsites.nonEmpty)
    assertTrue(taggedNewsitems.forall(i => i.`type` == "N"))
    assertTrue(taggedWebsites.forall(i => i.resource_tags.exists(t => t.tag_id == tag._id)))
  }

  @Test
  def canFilterByPublisher {
    val publisher = Await.result(mongoRepository.getWebsiteByUrlwords("wellington-city-council"), TenSeconds).get

    val publisherNewsitemsQuery = ResourceQuery(`type` = Some("N"), publisher = Some(publisher))
    val publisherNewsitems = queryForResources(publisherNewsitemsQuery)

    assertTrue(publisherNewsitems.nonEmpty)
    assertTrue(publisherNewsitems.forall(i => i.asInstanceOf[Newsitem].publisher.contains(publisher._id)))
  }

  @Test
  def canGetFeedsForPublisher {
    val publisher = Await.result(mongoRepository.getWebsiteByUrlwords("wellington-city-council"), TenSeconds).get

    val publisherFeedsQuery = ResourceQuery(`type` = Some("F"), publisher = Some(publisher))
    val publisherFeeds = queryForResources(publisherFeedsQuery)

    assertTrue(publisherFeeds.nonEmpty)
    assertTrue(publisherFeeds.forall(i => i.`type` == "F"))
    assertTrue(publisherFeeds.forall(i => i.asInstanceOf[Feed].publisher.contains(publisher._id)))
  }

  @Test
  def canGetAllPublisherIds: Unit = {
    val publisherIds = Await.result(elasticSearchIndexer.getAllPublishers, TenSeconds)
    assertTrue(publisherIds.nonEmpty)
  }

  @Test
  def canCreateNewsitemDateRanges {
    val archiveLinks = Await.result(elasticSearchIndexer.getArchiveMonths, TenSeconds)
    assertTrue(archiveLinks.nonEmpty)
  }

  @Test
  def canFilterNewsitemsByDateRange {
    val startOfMonth = new DateTime(2016, 2, 1, 0, 0)
    val interval = new Interval(startOfMonth, startOfMonth.plusMonths(1))

    val monthNewsitems = ResourceQuery(`type` = Some("N"), interval = Some(interval))
    val results = Await.result(elasticSearchIndexer.getResources(monthNewsitems), TenSeconds)

    import scala.concurrent.ExecutionContext.Implicits.global
    val newsitems = Await.result(Future.sequence(results._1.map(i => mongoRepository.getResourceByObjectId(i))), TenSeconds).flatten

    assertTrue(newsitems.nonEmpty)
    assertTrue(newsitems.forall{n =>
      interval.contains(n.date.get.getTime)
    })
  }

  @Test
  def canCountArchiveTypes: Unit = {
    val typeCounts = Await.result(elasticSearchIndexer.getArchiveCounts, TenSeconds)

    val typesFound = typeCounts.keys.toSet
    assertEquals(Set("W", "N", "F", "L"), typesFound)
  }

  private def queryForResources(query: ResourceQuery): Seq[Resource] = {
    Await.result(elasticSearchIndexer.getResources(query).flatMap { rs =>
      Future.sequence(rs._1.map(mongoRepository.getResourceByObjectId)).map(_.flatten)
    }, TenSeconds)
  }

}
