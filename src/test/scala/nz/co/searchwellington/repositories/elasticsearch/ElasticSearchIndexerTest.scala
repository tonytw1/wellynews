package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.ShowBrokenDecisionService
import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.HandTaggingDAO
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexerTest.indexTagsService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.{IndexTagsService, TaggingReturnsOfficerService}
import nz.co.searchwellington.urls.UrlParser
import org.joda.time.{DateTime, Interval}
import org.junit.jupiter.api.Assertions.{assertFalse, assertTrue}
import org.junit.jupiter.api.Test
import org.scalatest.concurrent.Eventually.{eventually, interval, _}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

object ElasticSearchIndexerTest {
  private val databaseAndIndexName = "wellynews-" + UUID.randomUUID().toString

  private val mongoHost = Option(System.getenv("MONGO_HOST")).getOrElse("localhost")
  private val mongoRepository = new MongoRepository(s"mongodb://$mongoHost:27017/" + databaseAndIndexName)

  private val elasticHost = Option(System.getenv("ELASTIC_HOST")).getOrElse("localhost")

  private val showBrokenDecisionService = new ShowBrokenDecisionService

  val taggingReturnsOfficerService = new TaggingReturnsOfficerService(new HandTaggingDAO(mongoRepository), mongoRepository)
  val indexTagsService = new IndexTagsService(taggingReturnsOfficerService)

  val elasticSearchIndexer = new ElasticSearchIndexer(showBrokenDecisionService, s"http://$elasticHost:9200",
    ElasticSearchIndexerTest.databaseAndIndexName)
}

class ElasticSearchIndexerTest extends ReasonableWaits {

  val mongoRepository: MongoRepository = ElasticSearchIndexerTest.mongoRepository
  val elasticSearchIndexer: ElasticSearchIndexer = ElasticSearchIndexerTest.elasticSearchIndexer

  private val loggedInUser = User(admin = true)
  private val allNewsitems = ResourceQuery(`type` = Some(Set("N")))

  @Test
  def canFilterByType(): Unit = {
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
  def canFilterByFeedAcceptancePolicy(): Unit = {
    val acceptedFeed = Feed(acceptance = FeedAcceptancePolicy.ACCEPT)
    Await.result(mongoRepository.saveResource(acceptedFeed), TenSeconds)
    val ignoredFeed = Feed(acceptance = FeedAcceptancePolicy.IGNORE)
    Await.result(mongoRepository.saveResource(ignoredFeed), TenSeconds)
    indexResources(Seq(acceptedFeed, ignoredFeed))

    def acceptedFeeds = queryForResources(ResourceQuery(`type` = Some(Set("F")), feedAcceptancePolicy = Some(FeedAcceptancePolicy.ACCEPT)))

    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(acceptedFeeds.size mustBe 1)
    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(acceptedFeeds.contains(acceptedFeed) mustBe true)
    assertFalse(acceptedFeeds.map(_.id).contains(ignoredFeed.id))
    assertTrue(acceptedFeeds.forall(i => i.asInstanceOf[Feed].acceptance == FeedAcceptancePolicy.ACCEPT))
  }

  @Test
  def canFilterByTag(): Unit = {
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

    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(taggedNewsitems.nonEmpty mustBe true)
    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(taggedNewsitems.size mustBe 1)

    assertTrue(taggedNewsitems.forall(i => i.`type` == "N"))
    assertTrue(taggedNewsitems.forall(i => i.resource_tags.exists(t => t.tag_id == tag._id)))

    val taggedWebsitesQuery = withTag.copy(`type` = Some(Set("W")))

    def taggedWebsites = queryForResources(taggedWebsitesQuery)

    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(taggedNewsitems.nonEmpty mustBe true)
    assertTrue(taggedWebsites.forall(i => i.`type` == "W"))
    assertTrue(taggedWebsites.forall(i => i.resource_tags.exists(t => t.tag_id == tag._id)))
  }

  @Test
  def canFilterByPublisher(): Unit = {
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

    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(publisherNewsitems.nonEmpty mustBe true)
    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(publisherNewsitems.contains(publishersNewsitem) mustBe true)
    assertTrue(publisherNewsitems.forall(i => i.asInstanceOf[Newsitem].publisher.contains(publisher._id)))
  }

  @Test
  def canGetFeedsForPublisher(): Unit = {
    val publisher = Website()
    Await.result(mongoRepository.saveResource(publisher), TenSeconds)
    val feed = Feed()
    Await.result(mongoRepository.saveResource(feed), TenSeconds)
    val publishersFeed = Feed(publisher = Some(publisher._id))
    Await.result(mongoRepository.saveResource(publishersFeed), TenSeconds)

    indexResources(Seq(publisher, publishersFeed))

    val publisherFeedsQuery = ResourceQuery(`type` = Some(Set("F")), publisher = Some(publisher))
    def publisherFeeds = queryForResources(publisherFeedsQuery)

    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(publisherFeeds.nonEmpty mustBe true)
    assertTrue(publisherFeeds.forall(i => i.`type` == "F"))
    assertTrue(publisherFeeds.forall(i => i.asInstanceOf[Feed].publisher.contains(publisher._id)))
  }

  @Test
  def canGetNewsitemMonthlyCounts(): Unit = {
    val newsitem = Newsitem(date = Some(new DateTime(2019, 1, 1, 0, 0, 0).toDate))
    Await.result(mongoRepository.saveResource(newsitem), TenSeconds)
    val anotherNewsitem = Newsitem(date = Some(new DateTime(2018, 3, 7, 0, 0, 0).toDate))
    Await.result(mongoRepository.saveResource(anotherNewsitem), TenSeconds)
    val yetAnotherNewsitem = Newsitem(date = Some(new DateTime(2017, 7, 8, 0, 0, 0).toDate))
    Await.result(mongoRepository.saveResource(yetAnotherNewsitem), TenSeconds)

    indexResources(Seq(newsitem, anotherNewsitem, yetAnotherNewsitem))

    def archiveLinks = Await.result(elasticSearchIndexer.createdMonthAggregationFor(allNewsitems, loggedInUser = Some(loggedInUser)), TenSeconds)

    def monthStrings = archiveLinks.map(_._1.getStart.toDate.toGMTString)

    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(monthStrings.contains("1 Jan 2019 00:00:00 GMT") mustBe true)
    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(monthStrings.contains("1 Mar 2018 00:00:00 GMT") mustBe true)
    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(monthStrings.contains("1 Jul 2017 00:00:00 GMT") mustBe true)
  }

  @Test
  def canGetNewsitemsAcceptedByDaysCounts(): Unit = {
    val acceptedNewsitem = Newsitem(
      date = Some(new DateTime(2022, 6, 1, 0, 0, 0).toDate),
      accepted = Some(new DateTime(2022, 6, 2, 11, 23, 5).toDate))
    Await.result(mongoRepository.saveResource(acceptedNewsitem), TenSeconds)
    val anotherAcceptedNewsitem = Newsitem(date = Some(new DateTime(2022, 6, 2, 0, 0, 0).toDate),
        accepted = Some(new DateTime(2022, 6, 2, 17, 23, 5).toDate)
    )
    Await.result(mongoRepository.saveResource(anotherAcceptedNewsitem), TenSeconds)

    indexResources(Seq(acceptedNewsitem, anotherAcceptedNewsitem))

    def acceptedCounts = Await.result(elasticSearchIndexer.createdAcceptedDateAggregationFor(allNewsitems, loggedInUser = Some(loggedInUser)), TenSeconds)

    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(acceptedCounts.headOption.map(_._1) mustBe Some("2022-06-02"))
    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(acceptedCounts.headOption.map(_._2) mustBe Some(2L))
  }

  @Test
  def canFilterNewsitemsByDateRange(): Unit = {
    val newsitem = Newsitem(date = Some(new DateTime(2016, 2, 10, 0, 0, 0).toDate))
    Await.result(mongoRepository.saveResource(newsitem), TenSeconds)
    val anotherNewsitem = Newsitem(date = Some(new DateTime(2016, 3, 1, 0, 0, 0).toDate))
    Await.result(mongoRepository.saveResource(anotherNewsitem), TenSeconds)

    indexResources(Seq(newsitem, anotherNewsitem))

    val startOfMonth = new DateTime(2016, 2, 1, 0, 0)
    val month = new Interval(startOfMonth, startOfMonth.plusMonths(1))

    def monthNewsitems = queryForResources(ResourceQuery(`type` = Some(Set("N")), interval = Some(month)))

    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(monthNewsitems.contains(newsitem) mustBe true)
    assertTrue(monthNewsitems.forall(n => month.contains(n.date.get.getTime)))
  }

  @Test
  def canCountArchiveTypes(): Unit = {
    val newsitem = Newsitem()
    Await.result(mongoRepository.saveResource(newsitem), TenSeconds)
    val website = Website()
    Await.result(mongoRepository.saveResource(website), TenSeconds)
    val watchlist = Watchlist()
    Await.result(mongoRepository.saveResource(watchlist), TenSeconds)
    val feed = Feed()
    Await.result(mongoRepository.saveResource(feed), TenSeconds)

    indexResources(Seq(website, newsitem, watchlist, feed))

    def typeCounts = Await.result(elasticSearchIndexer.getTypeCounts(Some(loggedInUser)), TenSeconds)
    def typesFound = typeCounts.keys.toSet

    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(Set("W", "N", "F", "L").equals(typesFound) mustBe true)
  }

  @Test
  def canFilterResourcesByHostname(): Unit = {
    val fooWebsite = Website(page = "http://foo.local")
    val barWebsite = Website(page = "http://bar.local")
    val fooNewsitem = Website(page = "http://foo.local/123")

    Await.result(mongoRepository.saveResource(fooWebsite), TenSeconds)
    Await.result(mongoRepository.saveResource(barWebsite), TenSeconds)
    Await.result(mongoRepository.saveResource(fooNewsitem), TenSeconds)

    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(Await.result(mongoRepository.getResourceByObjectId(fooWebsite._id), TenSeconds).nonEmpty mustBe true)
    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(Await.result(mongoRepository.getResourceByObjectId(barWebsite._id), TenSeconds).nonEmpty mustBe true)
    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(Await.result(mongoRepository.getResourceByObjectId(fooNewsitem._id), TenSeconds).nonEmpty mustBe true)

    indexResources(Seq(fooWebsite, barWebsite, fooNewsitem))

    def fooResources = queryForResources(ResourceQuery(hostname = Some("foo.local")))
    def barResources = queryForResources(ResourceQuery(hostname = Some("bar.local")))

    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(fooResources.contains(fooWebsite) mustBe true)
    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(fooResources.contains(fooNewsitem) mustBe true)
    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(barResources.contains(barWebsite) mustBe true)

    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(fooResources.size mustBe 2)
    eventually(timeout(TenSeconds), interval(TenMilliSeconds))(barResources.size mustBe 1)

    assertFalse(fooResources.contains(barWebsite))
    assertFalse(barResources.contains(fooWebsite))
  }

  private def indexResources(resources: Seq[Resource]) = {  // TODO push to real code
    def indexWithHandTaggings(resource: Resource) = {
      val eventualIndexTags = indexTagsService.getIndexTagsForResource(resource)
      val eventualGeocode = indexTagsService.getIndexGeocodeForResource(resource)
      val eventuallyIndexed = for {
        indexTags <- eventualIndexTags
        geocode <- eventualGeocode
      } yield {
        val handTags = resource.resource_tags.map(_.tag_id.stringify)
        (resource, indexTags.map(_._id.stringify), handTags, geocode, new UrlParser().extractHostnameFrom(resource.page))
      }
      Await.result(eventuallyIndexed, TenSeconds)
    }

    Await.result(elasticSearchIndexer.updateMultipleContentItems(resources.map(indexWithHandTaggings)), TenSeconds)
  }

  private def queryForResources(query: ResourceQuery, user: User = loggedInUser): Seq[Resource] = {
    Await.result(elasticSearchIndexer.getResources(query, loggedInUser = Some(user)).flatMap { rs =>
      Future.sequence(rs._1.map(mongoRepository.getResourceByObjectId)).map(_.flatten)
    }, TenSeconds)
  }

}
