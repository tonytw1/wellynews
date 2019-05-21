package nz.co.searchwellington.repositories.mongo

import java.util.UUID

import nz.co.searchwellington.model._
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.Test

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

class MongoRepositoryTest {

  val TenSeconds = Duration(10, SECONDS)

  val mongoRepository = new MongoRepository("mongodb://localhost:27017/wellynews")

  @Test
  def canPersistResources = {
    val newsitem = Newsitem(title = Some(testName))

    Await.result(mongoRepository.saveResource(newsitem), TenSeconds)

    val reloaded = Await.result(mongoRepository.getResourceByObjectId(newsitem._id), TenSeconds)
    assertTrue(reloaded.nonEmpty)
    assertEquals(newsitem.title.get, reloaded.get.title.get)
  }

  @Test
  def canPersistTags = {
    val tag = Tag(name = "Test " + UUID.randomUUID().toString)

    Await.result(mongoRepository.saveTag(tag), TenSeconds)

    val reloaded = Await.result(mongoRepository.getTagById(tag.id), TenSeconds)
    assertTrue(reloaded.nonEmpty)
    assertEquals(tag.name, reloaded.get.name)
  }

  @Test
  def canListAllFeeds {
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
  def canListAllTags {
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
  def canPersistTagParent = {
    val parentTag = Tag(name = "Test parent " + UUID.randomUUID().toString)
    Await.result(mongoRepository.saveTag(parentTag), TenSeconds)
    val tagWithParent = Tag(name = "Test " + UUID.randomUUID().toString, parent = Some(parentTag._id))
    Await.result(mongoRepository.saveTag(tagWithParent), TenSeconds)

    val reloaded = Await.result(mongoRepository.getTagByObjectId(tagWithParent._id), TenSeconds).get

    assertTrue(reloaded.parent.nonEmpty)
    assertEquals(parentTag._id, reloaded.parent.get)
  }

  @Test
  def canPersistTagGeocode = {
    val geocode = Geocode(osmId = Some(123), osmType = Some("N"))
    val tagWithGeocode = Tag(name = "Test " + UUID.randomUUID().toString, geocode = Some(geocode))
    Await.result(mongoRepository.saveTag(tagWithGeocode), TenSeconds)

    val reloaded = Await.result(mongoRepository.getTagByObjectId(tagWithGeocode._id), TenSeconds).get
    assertTrue(reloaded.geocode.nonEmpty)
    assertEquals(tagWithGeocode.geocode, reloaded.geocode)
  }

  @Test
  def canPersistResourceGeocode = {
    val resourceWithGeocode = Website(geocode = Some(Geocode(osmId = Some(123L), osmType = Some("N"))))
    Await.result(mongoRepository.saveResource(resourceWithGeocode), TenSeconds)

    val reloaded = Await.result(mongoRepository.getResourceByObjectId(resourceWithGeocode._id), TenSeconds).get
    assertTrue(reloaded.geocode.nonEmpty)
    assertEquals(resourceWithGeocode.geocode, reloaded.geocode)
  }

  @Test
  def canPersistTaggingsForResource = {
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
  def canFindResourcesByTag = {
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

  def canFindResourcesByTaggingUser = {
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
  def canUpdateResources = {
    val title = testName
    val newsitem = Newsitem(title = Some(title))
    Await.result(mongoRepository.saveResource(newsitem), TenSeconds)

    val updatedTitle = title + " updated"
    val updated = newsitem.copy(title = Some(updatedTitle), http_status = 200)
    Await.result(mongoRepository.saveResource(updated), TenSeconds)

    val reloaded = Await.result(mongoRepository.getResourceByObjectId(newsitem._id), TenSeconds).get
    assertEquals(updatedTitle, reloaded.title.get)
    assertEquals(200, reloaded.http_status)
  }

  @Test
  def canPersistNewsitemPublisher = {
    val publisher = Website(title = Some(testName))
    Await.result(mongoRepository.saveResource(publisher), TenSeconds)
    val newsitemWithPublisher = Newsitem(publisher = Some(publisher._id))
    Await.result(mongoRepository.saveResource(newsitemWithPublisher), TenSeconds)

    val reloaded = Await.result(mongoRepository.getResourceByObjectId(newsitemWithPublisher._id), TenSeconds)

    assertTrue(reloaded.get.asInstanceOf[Newsitem].publisher.nonEmpty)
    assertEquals(publisher._id, reloaded.get.asInstanceOf[Newsitem].publisher.get)
  }

  @Test
  def feedAcceptancePolicyCanBePersistedAsAEnum = {
    val feed = Feed(acceptance = FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES)
    Await.result(mongoRepository.saveResource(feed), TenSeconds)

    val reloaded = Await.result(mongoRepository.getResourceByObjectId(feed._id), TenSeconds).get.asInstanceOf[Feed]

    assertEquals(FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES, reloaded.acceptance)
  }

  @Test
  def canReadListOfResourceIds = {
    val publisher = Website(title = Some(testName))
    Await.result(mongoRepository.saveResource(publisher), TenSeconds)
    val newsitem = Newsitem(title = Some(testName))
    Await.result(mongoRepository.saveResource(newsitem), TenSeconds)
    val feed = Newsitem(title = Some(testName))
    Await.result(mongoRepository.saveResource(feed), TenSeconds)

    val resourceIds = Await.result(mongoRepository.getAllResourceIds(), TenSeconds)

    assertTrue(resourceIds.nonEmpty)
    assertTrue(resourceIds.contains(publisher._id))
    assertTrue(resourceIds.contains(newsitem._id))
    assertTrue(resourceIds.contains(feed._id))
  }

  @Test
  def canPersistUsers = {
    val user = User(name = Some("Test " + UUID.randomUUID().toString))

    Await.result(mongoRepository.saveUser(user), TenSeconds)

    val reloaded = Await.result(mongoRepository.getUserByObjectId(user._id), TenSeconds)
    assertTrue(reloaded.nonEmpty)
    assertEquals(user.name, reloaded.get.name)
  }

  @Test
  def canFindUsersByLinkedTwitterId = {
    val twitterId = Random.nextInt(Int.MaxValue)
    val user = User(twitterid = Some(twitterId))
    Await.result(mongoRepository.saveUser(user), TenSeconds)

    val reloaded = Await.result(mongoRepository.getUserByTwitterId(twitterId), TenSeconds)
    assertTrue(reloaded.nonEmpty)
    assertEquals(user, reloaded.get)
  }

  private def testName = "Test " + UUID.randomUUID.toString

}
