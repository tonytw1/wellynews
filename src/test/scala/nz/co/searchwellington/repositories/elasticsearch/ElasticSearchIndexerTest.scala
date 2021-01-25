package nz.co.searchwellington.repositories.elasticsearch

import java.util.UUID

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.ShowBrokenDecisionService
import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.HandTaggingDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import org.joda.time.{DateTime, Interval}
import org.junit.Assert.{assertFalse, assertTrue}
import org.junit.Test
import org.mockito.Mockito.{mock, when}
import org.scalatest.concurrent.Eventually.{eventually, interval, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class ElasticSearchIndexerTest extends ReasonableWaits {
  
  private val databaseAndIndexName = "wellynews-" + UUID.randomUUID().toString()

  private val mongoHost = {
    var mongoHost = System.getenv("MONGO_HOST");
    if (mongoHost == null) {
      mongoHost = "localhost";
    }
    mongoHost
  }

  val mongoRepository = new MongoRepository(s"mongodb://$mongoHost:27017/" + databaseAndIndexName)

  private val showBrokenDecisionService = mock(classOf[ShowBrokenDecisionService])
  val taggingReturnsOfficerService = new TaggingReturnsOfficerService(new HandTaggingDAO(mongoRepository), mongoRepository)

  private val elasticHost = {
    var mongoHost = System.getenv("ELASTIC_HOST");
    if (mongoHost == null) {
      mongoHost = "localhost";
    }
    mongoHost
  }

  val elasticSearchIndexer = new ElasticSearchIndexer(showBrokenDecisionService, s"http://$elasticHost:9200", databaseAndIndexName, taggingReturnsOfficerService)
  val rebuild = new ElasticSearchIndexRebuildService(mongoRepository, elasticSearchIndexer, taggingReturnsOfficerService)

  private val loggedInUser = User()

  {
    when(showBrokenDecisionService.shouldShowBroken(Some(loggedInUser))).thenReturn(true) // TODO This is in an awkward position
  }

  private val allNewsitems = ResourceQuery(`type` = Some(Set("N")))

  @Test
  def canFilterByType {
    val newsitem = Newsitem()
    Await.result(mongoRepository.saveResource(newsitem), TenSeconds)
    val website = Website()
    Await.result(mongoRepository.saveResource(website), TenSeconds)
    val feed = Feed()
    Await.result(mongoRepository.saveResource(feed), TenSeconds)

    indexResources(Seq(newsitem, website, feed))

    def newsitems = queryForResources(allNewsitems)
    eventually(timeout(TenSeconds), interval(TenMilliSeconds), newsitems.nonEmpty)
    assertTrue(newsitems.forall(i => i.`type` == "N"))

    def websites = queryForResources(ResourceQuery(`type` = Some(Set("W"))))
    eventually(timeout(TenSeconds), interval(TenMilliSeconds), websites.nonEmpty)
    assertTrue(websites.forall(i => i.`type` == "W"))

    def feeds = queryForResources(ResourceQuery(`type` = Some(Set("F"))))
    eventually(timeout(TenSeconds), interval(TenMilliSeconds), feeds.nonEmpty)
    assertTrue(feeds.forall(i => i.`type` == "F"))
  }

  @Test
  def canFilterByFeedAcceptancePolicy {
    val acceptedFeed = Feed(acceptance = FeedAcceptancePolicy.ACCEPT)
    Await.result(mongoRepository.saveResource(acceptedFeed), TenSeconds)
    val ignoredFeed = Feed(acceptance = FeedAcceptancePolicy.IGNORE)
    Await.result(mongoRepository.saveResource(ignoredFeed), TenSeconds)
    indexResources(Seq(acceptedFeed, ignoredFeed))

    def acceptedFeeds = queryForResources(ResourceQuery(`type` = Some(Set("F")), feedAcceptancePolicy = Some(FeedAcceptancePolicy.ACCEPT)))

    eventually(timeout(TenSeconds), interval(TenMilliSeconds), acceptedFeeds.size == 2)
    eventually(timeout(TenSeconds), interval(TenMilliSeconds), acceptedFeeds.contains(acceptedFeed))
    assertFalse(acceptedFeeds.map(_.id).contains(ignoredFeed.id))
    assertTrue(acceptedFeeds.forall(i => i.asInstanceOf[Feed].acceptance == FeedAcceptancePolicy.ACCEPT))
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
    val taggedNewsitemsQuery = withTag.copy(`type` = Some(Set("N")))

    def taggedNewsitems = queryForResources(taggedNewsitemsQuery)

    eventually(timeout(TenSeconds), interval(TenMilliSeconds), taggedNewsitems.nonEmpty)
    eventually(timeout(TenSeconds), interval(TenMilliSeconds), taggedNewsitems.size == 1)

    assertTrue(taggedNewsitems.forall(i => i.`type` == "N"))
    assertTrue(taggedNewsitems.forall(i => i.resource_tags.exists(t => t.tag_id == tag._id)))

    val taggedWebsitesQuery = withTag.copy(`type` = Some(Set("W")))

    def taggedWebsites = queryForResources(taggedWebsitesQuery)

    eventually(timeout(TenSeconds), interval(TenMilliSeconds), taggedNewsitems.nonEmpty)
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

    val publisherNewsitemsQuery = ResourceQuery(`type` = Some(Set("N")), publisher = Some(publisher))

    def publisherNewsitems = queryForResources(publisherNewsitemsQuery)

    eventually(timeout(TenSeconds), interval(TenMilliSeconds), publisherNewsitems.nonEmpty)
    eventually(timeout(TenSeconds), interval(TenMilliSeconds), publisherNewsitems.contains(publishersNewsitem))
    assertTrue(publisherNewsitems.forall(i => i.asInstanceOf[Newsitem].publisher.contains(publisher._id)))
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

    val publisherFeedsQuery = ResourceQuery(`type` = Some(Set("F")), publisher = Some(publisher))
    val publisherFeeds = queryForResources(publisherFeedsQuery)

    eventually(timeout(TenSeconds), interval(TenMilliSeconds), publisherFeeds.nonEmpty)
    assertTrue(publisherFeeds.forall(i => i.`type` == "F"))
    assertTrue(publisherFeeds.forall(i => i.asInstanceOf[Feed].publisher.contains(publisher._id)))
  }

  @Test
  def canGetNewsitemMonthlyCounts {
    val newsitem = Newsitem(date = Some(new DateTime(2019, 1, 1, 0, 0, 0).toDate))
    Await.result(mongoRepository.saveResource(newsitem), TenSeconds)
    val anotherNewsitem = Newsitem(date = Some(new DateTime(2018, 3, 7, 0, 0, 0).toDate))
    Await.result(mongoRepository.saveResource(anotherNewsitem), TenSeconds)
    val yetAnotherNewsitem = Newsitem(date = Some(new DateTime(2017, 7, 8, 0, 0, 0).toDate))
    Await.result(mongoRepository.saveResource(yetAnotherNewsitem), TenSeconds)

    indexResources(Seq(newsitem, anotherNewsitem, yetAnotherNewsitem))

    def archiveLinks = Await.result(elasticSearchIndexer.createdMonthAggregationFor(allNewsitems, loggedInUser = Some(loggedInUser)), TenSeconds)
    def monthStrings = archiveLinks.map(_._1.getStart.toDate.toString)

    // TODO Explain test flake
    //eventually(timeout(TenSeconds), interval(TenMilliSeconds), monthStrings.contains("Tue Jan 01 00:00:00 GMT 2019"))
    //eventually(timeout(TenSeconds), interval(TenMilliSeconds), monthStrings.contains("Thu Mar 01 00:00:00 GMT 2018"))
    //eventually(timeout(TenSeconds), interval(TenMilliSeconds), monthStrings.contains("Sat Jul 01 01:00:00 BST 2017"))
  }

  @Test
  def canFilterNewsitemsByDateRange {
    val newsitem = Newsitem(date = Some(new DateTime(2016, 2, 10, 0, 0, 0).toDate))
    Await.result(mongoRepository.saveResource(newsitem), TenSeconds)
    val anotherNewsitem = Newsitem(date = Some(new DateTime(2016, 3, 1, 0, 0, 0).toDate))
    Await.result(mongoRepository.saveResource(anotherNewsitem), TenSeconds)

    indexResources(Seq(newsitem, anotherNewsitem))

    val startOfMonth = new DateTime(2016, 2, 1, 0, 0)
    val month = new Interval(startOfMonth, startOfMonth.plusMonths(1))

    def monthNewsitems = queryForResources(ResourceQuery(`type` = Some(Set("N")), interval = Some(month)))

    eventually(timeout(TenSeconds), interval(TenMilliSeconds), monthNewsitems.contains(newsitem))
    assertTrue(monthNewsitems.forall(n => month.contains(n.date.get.getTime)))
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

    def typeCounts = Await.result(elasticSearchIndexer.getArchiveCounts(Some(loggedInUser)), TenSeconds)
    def typesFound = typeCounts.keys.toSet

    eventually(timeout(TenSeconds), interval(TenMilliSeconds), Set("W", "N", "F", "L").equals(typesFound))
  }

  @Test
  def canFilterResourcesByHostname: Unit = {
    val fooWebsite = Website(page = "http://foo.local")
    val barWebsite = Website(page = "http://bar.local")
    val fooNewsitem = Website(page = "http://foo.local/123")

    Await.result(mongoRepository.saveResource(fooWebsite), TenSeconds)
    Await.result(mongoRepository.saveResource(barWebsite), TenSeconds)
    Await.result(mongoRepository.saveResource(fooNewsitem), TenSeconds)

    eventually(timeout(TenSeconds), interval(TenMilliSeconds), Await.result(mongoRepository.getResourceByObjectId(fooWebsite._id), TenSeconds).nonEmpty)
    eventually(timeout(TenSeconds), interval(TenMilliSeconds), Await.result(mongoRepository.getResourceByObjectId(barWebsite._id), TenSeconds).nonEmpty)
    eventually(timeout(TenSeconds), interval(TenMilliSeconds), Await.result(mongoRepository.getResourceByObjectId(fooNewsitem._id), TenSeconds).nonEmpty)

    indexResources(Seq(fooWebsite, barWebsite, fooNewsitem))

    def fooResources = queryForResources(ResourceQuery(hostname = Some("foo.local")))
    def barResources = queryForResources(ResourceQuery(hostname = Some("bar.local")))

    eventually(timeout(TenSeconds), interval(TenMilliSeconds), fooResources.contains(fooWebsite))
    eventually(timeout(TenSeconds), interval(TenMilliSeconds), fooResources.contains(fooNewsitem))
    eventually(timeout(TenSeconds), interval(TenMilliSeconds), barResources.contains(barResources))

    eventually(timeout(TenSeconds), interval(TenMilliSeconds), fooResources.size == 2)
    eventually(timeout(TenSeconds), interval(TenMilliSeconds), barResources.size == 1)

    Thread.sleep(1000)  // TODO Something is not fixed after eventually passes for the first time
    assertFalse(fooResources.contains(barWebsite))
    assertFalse(barResources.contains(fooWebsite))
  }

  private def indexResources(resources: Seq[Resource]) = {
    def indexWithHandTaggings(resource: Resource) = (resource, resource.resource_tags.map(_.tag_id.stringify))

    Await.result(elasticSearchIndexer.updateMultipleContentItems(resources.map(indexWithHandTaggings)), TenSeconds)
  }

  private def queryForResources(query: ResourceQuery, user: User = loggedInUser): Seq[Resource] = {
    Await.result(elasticSearchIndexer.getResources(query, loggedInUser = Some(user)).flatMap { rs =>
      Future.sequence(rs._1.map(mongoRepository.getResourceByObjectId)).map(_.flatten)
    }, TenSeconds)
  }

}
