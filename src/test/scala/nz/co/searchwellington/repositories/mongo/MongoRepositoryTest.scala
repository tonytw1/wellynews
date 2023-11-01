package nz.co.searchwellington.repositories.mongo

import java.util.UUID
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.geo.{Geocode, LatLong, OsmId}
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse, assertTrue}
import org.junit.jupiter.api.Test

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

object MongoRepositoryTest {
  private val databaseName = "wellynews-" + UUID.randomUUID().toString
  private val mongoHost = Option(System.getenv("MONGO_HOST")).getOrElse("localhost")

  private val mongoRepository = new MongoRepository(s"mongodb://$mongoHost:27017/" + databaseName)
}

class MongoRepositoryTest extends ReasonableWaits {

  private val mongoRepository = MongoRepositoryTest.mongoRepository

  @Test
  def canPersistResources(): Unit = {
    val newsitem = Newsitem(title = testName, httpStatus = Some(HttpStatus(200, redirecting = true)))

    Await.result(mongoRepository.saveResource(newsitem), TenSeconds)

    val reloaded = Await.result(mongoRepository.getResourceByObjectId(newsitem._id), TenSeconds)
    assertTrue(reloaded.nonEmpty)
    assertEquals(newsitem.title, reloaded.get.title)
    assertTrue(reloaded.get.httpStatus.get.redirecting)
  }

  @Test
  def canFetchBatchOfResoucesById(): Unit = {
    val newsitem = Newsitem(title = testName)
    val anotherNewsitem = Newsitem(title = testName)
    val yetAnotherNewsitem = Newsitem(title = testName)
    val resources = Seq(newsitem, anotherNewsitem, yetAnotherNewsitem)

    resources.foreach {resource =>
        Await.result(mongoRepository.saveResource(resource), TenSeconds)
    }
    val ids = resources.map(_._id)

    val reread = Await.result(mongoRepository.getResourcesByObjectIds(ids), TenSeconds)

    assertEquals(3, reread.size)
  }

  @Test
  def canPersistTags(): Unit = {
    val tag = Tag(name = "Test " + UUID.randomUUID().toString)

    Await.result(mongoRepository.saveTag(tag), TenSeconds)

    val reloaded = Await.result(mongoRepository.getTagById(tag.id), TenSeconds)
    assertTrue(reloaded.nonEmpty)
    assertEquals(tag.name, reloaded.get.name)
  }

  @Test
  def canPersistUsers(): Unit = {
    val user = User(name = Some("Test " + UUID.randomUUID().toString), twitterid = Some(123))

    Await.result(mongoRepository.saveUser(user), TenSeconds)

    val reloaded = Await.result(mongoRepository.getUserByObjectId(user._id), TenSeconds)
    assertTrue(reloaded.nonEmpty)
    assertEquals(user.name, reloaded.get.name)
  }

  @Test
  def canListAllFeeds(): Unit = {
    val feed = Feed()
    Await.result(mongoRepository.saveResource(feed), TenSeconds)
    val anotherFeed = Feed()
    Await.result(mongoRepository.saveResource(anotherFeed), TenSeconds)

    val reloaded = Await.result(mongoRepository.getAllFeeds(), TenSeconds)
    assertTrue(reloaded.nonEmpty)
    assertTrue(reloaded.contains(feed))
    assertTrue(reloaded.contains(anotherFeed))
  }

  @Test
  def canListAllTags(): Unit = {
    val tag = Tag(name = "Test " + UUID.randomUUID().toString)
    Await.result(mongoRepository.saveTag(tag), TenSeconds)
    val anotherTag = Tag(name = "Test " + UUID.randomUUID().toString)
    Await.result(mongoRepository.saveTag(anotherTag), TenSeconds)

    val reloaded = Await.result(mongoRepository.getAllTags(), TenSeconds)
    assertTrue(reloaded.nonEmpty)
    assertTrue(reloaded.contains(tag))
    assertTrue(reloaded.contains(anotherTag))
  }

  @Test
  def canPersistTagParent(): Unit = {
    val parentTag = Tag(name = "Test parent " + UUID.randomUUID().toString)
    Await.result(mongoRepository.saveTag(parentTag), TenSeconds)
    val tagWithParent = Tag(name = "Test " + UUID.randomUUID().toString, parent = Some(parentTag._id))
    Await.result(mongoRepository.saveTag(tagWithParent), TenSeconds)

    val reloaded = Await.result(mongoRepository.getTagByObjectId(tagWithParent._id), TenSeconds).get

    assertTrue(reloaded.parent.nonEmpty)
    assertEquals(parentTag._id, reloaded.parent.get)
  }

  @Test
  def canPersistTagGeocode(): Unit = {
    val osmId = OsmId(id = 123L, `type` = "N")
    val geocode = Geocode(osmId = Some(osmId), latLong = Some(LatLong(latitude = 50.0, longitude = -0.1)))
    val tagWithGeocode = Tag(name = "Test " + UUID.randomUUID().toString, geocode = Some(geocode))
    Await.result(mongoRepository.saveTag(tagWithGeocode), TenSeconds)

    val reloaded = Await.result(mongoRepository.getTagByObjectId(tagWithGeocode._id), TenSeconds).get
    assertTrue(reloaded.geocode.nonEmpty)
    assertEquals(tagWithGeocode.geocode, reloaded.geocode)
  }

  @Test
  def canPersistResourceGeocode(): Unit = {
    val osmId = OsmId(id = 123L, `type` = "N")
    val resourceWithGeocode = Website(geocode = Some(Geocode(osmId = Some(osmId))))
    Await.result(mongoRepository.saveResource(resourceWithGeocode), TenSeconds)

    val reloaded = Await.result(mongoRepository.getResourceByObjectId(resourceWithGeocode._id), TenSeconds).get
    assertTrue(reloaded.geocode.nonEmpty)
    assertEquals(resourceWithGeocode.geocode, reloaded.geocode)
  }

  @Test
  def canPersistTaggingsForResource(): Unit = {
    val tag = Tag()
    Await.result(mongoRepository.saveTag(tag), TenSeconds)
    val taggingUser = User()
    Await.result(mongoRepository.saveUser(taggingUser), TenSeconds)
    val taggedResource = Website(resource_tags = Seq(Tagging(tag_id = tag._id, user_id = taggingUser._id)))
    Await.result(mongoRepository.saveResource(taggedResource), TenSeconds)

    val reloaded = Await.result(mongoRepository.getResourceByObjectId(taggedResource._id), TenSeconds).get
    assertEquals(1, reloaded.resource_tags.size)
    assertEquals(tag._id, reloaded.resource_tags.head.tag_id)
    assertEquals(taggingUser._id, reloaded.resource_tags.head.user_id)
  }

  @Test
  def canFindResourcesByTag(): Unit = {
    val taggingUser = User()
    Await.result(mongoRepository.saveUser(taggingUser), TenSeconds)

    val tag = Tag()
    Await.result(mongoRepository.saveTag(tag), TenSeconds)
    val anotherTag = Tag()
    Await.result(mongoRepository.saveTag(anotherTag), TenSeconds)

    val taggedResource = Website(resource_tags = Seq(Tagging(tag_id = tag._id, user_id = taggingUser._id)))
    Await.result(mongoRepository.saveResource(taggedResource), TenSeconds)
    val anotherTaggedResource = Website(resource_tags = Seq(Tagging(tag_id = anotherTag._id, user_id = taggingUser._id)))
    Await.result(mongoRepository.saveResource(anotherTaggedResource), TenSeconds)

    val taggedResourceIds = Await.result(mongoRepository.getResourceIdsByTag(tag), TenSeconds)

    assertTrue(taggedResourceIds.nonEmpty)
    assertTrue(taggedResourceIds.contains(taggedResource._id))
    assertFalse(taggedResourceIds.contains(anotherTaggedResource._id))

    val taggedResources = taggedResourceIds.flatMap { oid =>
      Await.result(mongoRepository.getResourceByObjectId(oid), TenSeconds)
    }
    assertTrue(taggedResources.forall { r =>
      r.resource_tags.exists(t => t.tag_id == tag._id)
    })
  }

  @Test
  def canFindResourcesByTaggingUser(): Unit = {
    val taggingUser = User()
    Await.result(mongoRepository.saveUser(taggingUser), TenSeconds)
    val anotherTaggingUser = User()
    Await.result(mongoRepository.saveUser(anotherTaggingUser), TenSeconds)

    val tag = Tag()
    Await.result(mongoRepository.saveTag(tag), TenSeconds)

    val resourceTaggedByUser = Website(resource_tags = Seq(Tagging(tag_id = tag._id, user_id = taggingUser._id)))
    Await.result(mongoRepository.saveResource(resourceTaggedByUser), TenSeconds)
    val resourceTaggedByAnotherUser = Website(resource_tags = Seq(Tagging(tag_id = tag._id, user_id = anotherTaggingUser._id)))
    Await.result(mongoRepository.saveResource(resourceTaggedByAnotherUser), TenSeconds)

    val taggedResourceIds = Await.result(mongoRepository.getResourceIdsByTaggingUser(anotherTaggingUser), TenSeconds)

    assertTrue(taggedResourceIds.nonEmpty)
    assertEquals(1, taggedResourceIds.size)
    assertEquals(resourceTaggedByAnotherUser._id, taggedResourceIds.head)
  }

  @Test
  def canUpdateResources(): Unit = {
    val title = testName
    val newsitem = Newsitem(title = title)
    Await.result(mongoRepository.saveResource(newsitem), TenSeconds)

    val updatedTitle = title + " updated"
    val updated = newsitem.copy(title = updatedTitle, httpStatus = Some(HttpStatus(200)))
    Await.result(mongoRepository.saveResource(updated), TenSeconds)

    val reloaded = Await.result(mongoRepository.getResourceByObjectId(newsitem._id), TenSeconds).get

    assertEquals(updatedTitle, reloaded.title)
    assertEquals(200, reloaded.httpStatus.get.status)
  }

  @Test
  def canUpdateResourceLastScanned(): Unit = {
    val title = testName
    val newsitem = Newsitem(title = title)
    Await.result(mongoRepository.saveResource(newsitem), TenSeconds)
    val lastScanned = DateTime.now.minusWeeks(1).toDate

    Await.result(mongoRepository.setLastScanned(newsitem._id, lastScanned), TenSeconds)

    val reloaded = Await.result(mongoRepository.getResourceByObjectId(newsitem._id), TenSeconds).get
    assertEquals(title, reloaded.title)
    assertEquals(Some(lastScanned), reloaded.last_scanned)
  }

  @Test
  def canPersistNewsitemPublisher(): Unit = {
    val publisher = Website(title = testName)
    Await.result(mongoRepository.saveResource(publisher), TenSeconds)
    val newsitemWithPublisher = Newsitem(publisher = Some(publisher._id))
    Await.result(mongoRepository.saveResource(newsitemWithPublisher), TenSeconds)

    val reloaded = Await.result(mongoRepository.getResourceByObjectId(newsitemWithPublisher._id), TenSeconds)

    assertTrue(reloaded.get.asInstanceOf[Newsitem].publisher.nonEmpty)
    assertEquals(publisher._id, reloaded.get.asInstanceOf[Newsitem].publisher.get)
  }

  @Test
  def feedAcceptancePolicyCanBePersistedAsAEnum(): Unit = {
    val feed = Feed(acceptance = FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES)
    Await.result(mongoRepository.saveResource(feed), TenSeconds)

    val reloaded = Await.result(mongoRepository.getResourceByObjectId(feed._id), TenSeconds).get.asInstanceOf[Feed]

    assertEquals(FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES, reloaded.acceptance)
  }

  @Test
  def canObtainListOfAllResourceIds(): Unit = {
    val publisher = Website(title = testName)
    Await.result(mongoRepository.saveResource(publisher), TenSeconds)
    val newsitem = Newsitem(title = testName)
    Await.result(mongoRepository.saveResource(newsitem), TenSeconds)
    val feed = Newsitem(title = testName)
    Await.result(mongoRepository.saveResource(feed), TenSeconds)

    val resourceIds = Await.result(mongoRepository.getAllResourceIds(), TenSeconds)

    assertTrue(resourceIds.nonEmpty)
    assertTrue(resourceIds.contains(publisher._id))
    assertTrue(resourceIds.contains(newsitem._id))
    assertTrue(resourceIds.contains(feed._id))
  }

  @Test
  def canFindUsersByLinkedTwitterId(): Unit = {
    val twitterId = Random.nextInt(Int.MaxValue)
    val user = User(twitterid = Some(twitterId))
    Await.result(mongoRepository.saveUser(user), TenSeconds)

    val reloaded = Await.result(mongoRepository.getUserByTwitterId(twitterId), TenSeconds)
    assertTrue(reloaded.nonEmpty)
    assertEquals(user, reloaded.get)
  }

  @Test
  def canPersistSnapshots(): Unit = {
    val snapshot = Snapshot("https://localhost/a-page.html", "Some page content", DateTime.now.toDate)

    Await.result(mongoRepository.saveSnapshot(snapshot), TenSeconds)

    val reloaded = Await.result(mongoRepository.getSnapshotByUrl(snapshot.url), TenSeconds)
    assertTrue(reloaded.nonEmpty)
    assertEquals(snapshot, reloaded.get)
  }

  private def testName = "Test " + UUID.randomUUID.toString

}
