package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.controllers.ShowBrokenDecisionService
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, Newsitem, Resource, Watchlist, Website}
import nz.co.searchwellington.repositories.HandTaggingDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import org.joda.time.{DateTime, Interval}
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.when

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ElasticSearchIndexerTest {

  private val showBrokenDecisionService = mock(classOf[ShowBrokenDecisionService])

  val mongoRepository = new MongoRepository("mongodb://localhost:27017/searchwellington")
  val elasticSearchIndexer = new ElasticSearchIndexer(showBrokenDecisionService, "localhost", 9200)
  val taggingReturnsOfficerService = new TaggingReturnsOfficerService(new HandTaggingDAO(mongoRepository), mongoRepository)

  val rebuild = new ElasticSearchIndexRebuildService(mongoRepository, elasticSearchIndexer, taggingReturnsOfficerService)

  private val TenSeconds = Duration(10, SECONDS)

  {
    when(showBrokenDecisionService.shouldShowBroken()).thenReturn(true) // TODO This is in an awkward position
  }

  @Test
  def canFilterByType {
    val newsitem = Newsitem()
    Await.result(mongoRepository.saveResource(newsitem), TenSeconds)
    val website = Website()
    Await.result(mongoRepository.saveResource(website), TenSeconds)
    val feed = Feed()
    Await.result(mongoRepository.saveResource(feed), TenSeconds)

    Await.result(elasticSearchIndexer.updateMultipleContentItems(Seq(
      (newsitem, Seq.empty),
      (website, Seq.empty),
      (feed, Seq.empty)
    )), TenSeconds)
    Thread.sleep(1000)

    val newsitems = Await.result(elasticSearchIndexer.getResources(ResourceQuery(`type` = Some("N"))), TenSeconds)
    assertTrue(newsitems._1.nonEmpty)
    assertTrue(newsitems._1.forall(i => Await.result(mongoRepository.getResourceByObjectId(i), TenSeconds).get.`type` == "N"))

    val websites = Await.result(elasticSearchIndexer.getResources(ResourceQuery(`type` = Some("W"))), TenSeconds)
    assertTrue(websites._1.nonEmpty)
    assertTrue(websites._1.forall(i => Await.result(mongoRepository.getResourceByObjectId(i), TenSeconds).get.`type` == "W"))

    val feeds = Await.result(elasticSearchIndexer.getResources(ResourceQuery(`type` = Some("F"))), TenSeconds)
    assertTrue(feeds._1.nonEmpty)
    assertTrue(feeds._1.forall(i => Await.result(mongoRepository.getResourceByObjectId(i), TenSeconds).get.`type` == "F"))
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
    val publisher = Website()
    Await.result(mongoRepository.saveResource(publisher), TenSeconds)
    val anotherPublisher = Website()
    Await.result(mongoRepository.saveResource(anotherPublisher), TenSeconds)

    val publishersNewsitem = Newsitem(publisher = Some(publisher._id))
    Await.result(mongoRepository.saveResource(publishersNewsitem), TenSeconds)
    val anotherPublishersNewsitem = Newsitem(publisher = Some(anotherPublisher._id))
    Await.result(mongoRepository.saveResource(anotherPublishersNewsitem), TenSeconds)

    Await.result(elasticSearchIndexer.updateMultipleContentItems(Seq(
      (publishersNewsitem, Seq.empty),
      (anotherPublishersNewsitem, Seq.empty)
    )), TenSeconds)
    Thread.sleep(1000)

    val publisherNewsitemsQuery = ResourceQuery(`type` = Some("N"), publisher = Some(publisher))
    val publisherNewsitems = queryForResources(publisherNewsitemsQuery)

    assertTrue(publisherNewsitems.nonEmpty)
    assertTrue(publisherNewsitems.forall(i => i.asInstanceOf[Newsitem].publisher.contains(publisher._id)))
    assertTrue(publisherNewsitems.map(_._id).contains(publishersNewsitem._id))
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
  def canGetIdsOfAllPublishersWithAtLeastOneNewsitem: Unit = {  // TODO a feed some count as well
    val publisher = Website()
    Await.result(mongoRepository.saveResource(publisher), TenSeconds)
    val newsitem = Newsitem(publisher = Some(publisher._id))
    Await.result(mongoRepository.saveResource(newsitem), TenSeconds)

    Await.result(elasticSearchIndexer.updateMultipleContentItems(Seq(
      (newsitem, Seq.empty)
    )), TenSeconds)
    Thread.sleep(1000)

    val publisherIds = Await.result(elasticSearchIndexer.getAllPublishers, TenSeconds)

    assertTrue(publisherIds.nonEmpty)
    assertTrue(publisherIds.contains(publisher._id.stringify))
  }

  @Test
  def canObtainsNewsitemArchiveMonths {
    val newsitem = Newsitem(date = Some(new DateTime(2019, 1, 1, 0, 0, 0).toDate))
    Await.result(mongoRepository.saveResource(newsitem), TenSeconds)
    val anotherNewsitem = Newsitem(date = Some(new DateTime(2018, 3, 7, 0, 0, 0).toDate))
    Await.result(mongoRepository.saveResource(anotherNewsitem), TenSeconds)
    val yetAnotherNewsitem = Newsitem(date = Some(new DateTime(2017, 7, 8, 0, 0, 0).toDate))
    Await.result(mongoRepository.saveResource(yetAnotherNewsitem), TenSeconds)

    Await.result(elasticSearchIndexer.updateMultipleContentItems(Seq(
      (newsitem, Seq.empty),
      (anotherNewsitem, Seq.empty),
      (yetAnotherNewsitem, Seq.empty)
    )), TenSeconds)
    Thread.sleep(1000)

    val archiveLinks = Await.result(elasticSearchIndexer.getArchiveMonths, TenSeconds)

    assertTrue(archiveLinks.nonEmpty)
    val monthStrings = archiveLinks.map(_.getMonth.toString)
    assertTrue(monthStrings.contains("Tue Jan 01 00:00:00 GMT 2019"))
    assertTrue(monthStrings.contains("Thu Mar 01 00:00:00 GMT 2018"))
    assertTrue(monthStrings.contains("Sat Jul 01 01:00:00 BST 2017"))
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
    val newsitem = Newsitem()
    Await.result(mongoRepository.saveResource(newsitem), TenSeconds)
    val website = Website()
    Await.result(mongoRepository.saveResource(website), TenSeconds)
    val watchlist = Watchlist()
    Await.result(mongoRepository.saveResource(watchlist), TenSeconds)
    val feed = Feed()
    Await.result(mongoRepository.saveResource(feed), TenSeconds)

    Await.result(elasticSearchIndexer.updateMultipleContentItems(Seq(
      (website, Seq.empty),
      (newsitem, Seq.empty),
      (watchlist, Seq.empty),
      (feed, Seq.empty)
    )), TenSeconds)
    Thread.sleep(1000)

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
