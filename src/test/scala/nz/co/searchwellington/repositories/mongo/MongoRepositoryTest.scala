package nz.co.searchwellington.repositories.mongo

import java.util.UUID

import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, Geocode, Newsitem, Tag, User, Website}
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test

import scala.concurrent.Await
import scala.concurrent.duration._

class MongoRepositoryTest {

  val TenSeconds = Duration(10, SECONDS)

  val mongoRepository = new MongoRepository("mongodb://localhost:27017/wellynews")

  @Test
  def canPersistResources = {
    val newsitem = Newsitem(title = Some("Test " + UUID.randomUUID.toString))

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
  def canReadTagGeocode = {
    val geocode = Geocode(osmId = Some(123), osmType = Some("N"))
    val tagWithGeocode = Tag(name = "Test " + UUID.randomUUID().toString, geocode = Some(geocode))
    Await.result(mongoRepository.saveTag(tagWithGeocode), TenSeconds)

    val reloaded = Await.result(mongoRepository.getTagByObjectId(tagWithGeocode._id), TenSeconds).get
    assertTrue(reloaded.geocode.nonEmpty)
    assertEquals(tagWithGeocode.geocode, reloaded.geocode)
  }

  @Test
  def canUpdateResources = {
    val title = "Test " + UUID.randomUUID.toString
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
    val publisher = Website(title = Some("Test " + UUID.randomUUID.toString))
    Await.result(mongoRepository.saveResource(publisher), TenSeconds)
    val newsitemWithPublisher = Newsitem(publisher = Some(publisher._id))
    Await.result(mongoRepository.saveResource(publisher), TenSeconds)

    val reloaded = Await.result(mongoRepository.getResourceByObjectId(newsitemWithPublisher._id), TenSeconds)

    assertTrue(newsitemWithPublisher.asInstanceOf[Newsitem].publisher.nonEmpty)
    assertEquals(publisher._id, newsitemWithPublisher.asInstanceOf[Newsitem].publisher.get)
  }

  @Test
  def canReadResourceGeocode = {
    val resourceWithGeocode = Await.result(mongoRepository.getWebsiteByName("Aro Valley Community Centre"), TenSeconds).get
    assertTrue(resourceWithGeocode.geocode.nonEmpty)
    assertEquals("Aro Valley Community Centre, 48, Aro Street, Aro Valley, Wellington, 6021, New Zealand/Aotearoa", resourceWithGeocode.geocode.get.address.get)
  }

  @Test
  def canConnectToMongoAndReadFeeds {
    val feeds = Await.result(mongoRepository.getAllFeeds(), TenSeconds)
    assertEquals(414, feeds.size)
  }

  @Test
  def feedAcceptancePolicyCanBePersistedAsAEnum = {
    val feed = Feed(acceptance = FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES)
    Await.result(mongoRepository.saveResource(feed), TenSeconds)

    val reloaded = Await.result(mongoRepository.getResourceByObjectId(feed._id), TenSeconds).get.asInstanceOf[Feed]

    assertEquals(FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES, reloaded.acceptance)
  }

  @Test
  def canReadTaggingsForResource = {
    val taggedResource = Await.result(mongoRepository.getResourceByUrl("http://www.kitesurfers.co.nz/"), TenSeconds).get

    val taggings = taggedResource.resource_tags

    assertEquals(1, taggings.size)
    val tag = Await.result(mongoRepository.getTagByObjectId(taggings.head.tag_id), TenSeconds).get
    assertEquals("sport", tag.name)
  }

  @Test
  def canFindResourcesByTag = {
    val tag = Await.result(mongoRepository.getTagByUrlWords("arovalley"), TenSeconds).get

    val taggedResourceIds = Await.result(mongoRepository.getResourceIdsByTag(tag), TenSeconds)

    assertTrue(taggedResourceIds.nonEmpty)
    val taggedResources = taggedResourceIds.flatMap { oid =>
      Await.result(mongoRepository.getResourceByObjectId(oid), TenSeconds)
    }
    assertTrue(taggedResources.forall{ r =>
      r.resource_tags.exists(t => t.tag_id == tag._id)
    })
  }

  @Test
  def canReadListOfResourceIds = {
    val resourceIds = Await.result(mongoRepository.getAllResourceIds(), TenSeconds)
    assertEquals(88657, resourceIds.size)
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
    val user = Await.result(mongoRepository.getUserByTwitterId(14497864), TenSeconds)
    assertTrue(user.nonEmpty)
  }

}
