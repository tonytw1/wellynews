package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.controllers.ShowBrokenDecisionService
import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.HandTaggingDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import org.joda.time.{DateTime, Interval}
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
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

    indexResources(Seq(newsitem, website, feed))

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
    val acceptedFeed = Feed(acceptance = FeedAcceptancePolicy.ACCEPT)
    Await.result(mongoRepository.saveResource(acceptedFeed), TenSeconds)
    val ignoredFeed = Feed(acceptance = FeedAcceptancePolicy.IGNORE)
    Await.result(mongoRepository.saveResource(ignoredFeed), TenSeconds)

    indexResources(Seq(acceptedFeed, ignoredFeed))

    val acceptedFeeds = Await.result(elasticSearchIndexer.getResources(ResourceQuery(`type` = Some("F"),
      feedAcceptancePolicy = Some(FeedAcceptancePolicy.ACCEPT))), TenSeconds)
    assertTrue(acceptedFeeds._1.nonEmpty)
    assertTrue(acceptedFeeds._1.contains(acceptedFeed._id))
    assertFalse(acceptedFeeds._1.contains(ignoredFeed._id))
    assertTrue(acceptedFeeds._1.forall(i => Await.result(mongoRepository.getResourceByObjectId(i), TenSeconds).get.
      asInstanceOf[Feed].acceptance == FeedAcceptancePolicy.ACCEPT))
  }

  @Test
  def canFilterByTag {
    val tag = Tag()
    Await.result(mongoRepository.saveTag(tag), TenSeconds)
    val taggingUser = User()
    Await.result(mongoRepository.saveUser(taggingUser), TenSeconds)

    val newsitem = Newsitem()
    Await.result(mongoRepository.saveResource(newsitem), TenSeconds)
    val taggedNewsitem = Newsitem(resource_tags = Seq(Tagging(tag_id = tag._id, user_id = taggingUser._id)))
    Await.result(mongoRepository.saveResource(taggedNewsitem), TenSeconds)

    val website = Website()
    Await.result(mongoRepository.saveResource(website), TenSeconds)
    val taggedWebsite = Website(resource_tags = Seq(Tagging(tag_id = tag._id, user_id = taggingUser._id)))
    Await.result(mongoRepository.saveResource(taggedWebsite), TenSeconds)

    indexResources(Seq(newsitem, website, taggedNewsitem, taggedWebsite))

    val withTag = ResourceQuery(tags = Some(Set(tag)))
    val taggedNewsitemsQuery = withTag.copy(`type` = Some("N"))
    val taggedNewsitems = queryForResources(taggedNewsitemsQuery)
    assertTrue(taggedNewsitems.nonEmpty)
    assertTrue(taggedNewsitems.forall(i => i.`type` == "N"))
    assertTrue(taggedNewsitems.forall(i => i.resource_tags.exists(t => t.tag_id == tag._id)))

    val taggedWebsitesQuery = withTag.copy(`type` = Some("W"))
    val taggedWebsites = queryForResources(taggedWebsitesQuery)
    assertTrue(taggedWebsites.nonEmpty)
    assertTrue(taggedWebsites.forall(i => i.`type` == "W"))
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

    indexResources(Seq(publishersNewsitem, anotherPublishersNewsitem))

    val publisherNewsitemsQuery = ResourceQuery(`type` = Some("N"), publisher = Some(publisher))
    val publisherNewsitems = queryForResources(publisherNewsitemsQuery)

    assertTrue(publisherNewsitems.nonEmpty)
    assertTrue(publisherNewsitems.forall(i => i.asInstanceOf[Newsitem].publisher.contains(publisher._id)))
    assertTrue(publisherNewsitems.map(_._id).contains(publishersNewsitem._id))
  }

  @Test
  def canGetFeedsForPublisher {
    val publisher = Website()
    Await.result(mongoRepository.saveResource(publisher), TenSeconds)
    val feed = Feed()
    Await.result(mongoRepository.saveResource(feed), TenSeconds)
    val publishersFeed = Feed(publisher = Some(publisher._id))
    Await.result(mongoRepository.saveResource(publishersFeed), TenSeconds)

    indexResources(Seq(publisher, publishersFeed))

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

    indexResources(Seq(publisher, newsitem))

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

    indexResources(Seq(newsitem, anotherNewsitem, yetAnotherNewsitem))

    val archiveLinks = Await.result(elasticSearchIndexer.getArchiveMonths, TenSeconds)

    assertTrue(archiveLinks.nonEmpty)
    val monthStrings = archiveLinks.map(_.getMonth.toString)
    assertTrue(monthStrings.contains("Tue Jan 01 00:00:00 GMT 2019"))
    assertTrue(monthStrings.contains("Thu Mar 01 00:00:00 GMT 2018"))
    assertTrue(monthStrings.contains("Sat Jul 01 01:00:00 BST 2017"))
  }

  @Test
  def canFilterNewsitemsByDateRange {
    val newsitem = Newsitem(date = Some(new DateTime(2016, 2, 10, 0, 0, 0).toDate))
    Await.result(mongoRepository.saveResource(newsitem), TenSeconds)
    val anotherNewsitem = Newsitem(date = Some(new DateTime(2016, 3, 1, 0, 0, 0).toDate))
    Await.result(mongoRepository.saveResource(anotherNewsitem), TenSeconds)

    indexResources(Seq(newsitem, anotherNewsitem))

    val startOfMonth = new DateTime(2016, 2, 1, 0, 0)
    val interval = new Interval(startOfMonth, startOfMonth.plusMonths(1))
    val monthNewsitems = ResourceQuery(`type` = Some("N"), interval = Some(interval))

    val results = Await.result(elasticSearchIndexer.getResources(monthNewsitems), TenSeconds)

    import scala.concurrent.ExecutionContext.Implicits.global
    val newsitemsInInterval = Await.result(Future.sequence(results._1.map(i => mongoRepository.getResourceByObjectId(i))), TenSeconds).flatten

    assertTrue(newsitemsInInterval.nonEmpty)
    assertTrue(newsitemsInInterval.contains(newsitem))
    assertTrue(newsitemsInInterval.forall{n =>
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

    indexResources(Seq(website, newsitem, watchlist, feed))

    val typeCounts = Await.result(elasticSearchIndexer.getArchiveCounts, TenSeconds)

    val typesFound = typeCounts.keys.toSet
    assertEquals(Set("W", "N", "F", "L"), typesFound)
  }

  private def indexResources(resources: Seq[Resource]) = {
    def indexWithHandTaggings(resource: Resource) = (resource, resource.resource_tags.map(_.tag_id.stringify))
    Await.result(elasticSearchIndexer.updateMultipleContentItems(resources.map(indexWithHandTaggings)), TenSeconds)
    Thread.sleep(1000)
  }

  private def queryForResources(query: ResourceQuery): Seq[Resource] = {
    Await.result(elasticSearchIndexer.getResources(query).flatMap { rs =>
      Future.sequence(rs._1.map(mongoRepository.getResourceByObjectId)).map(_.flatten)
    }, TenSeconds)
  }

}
