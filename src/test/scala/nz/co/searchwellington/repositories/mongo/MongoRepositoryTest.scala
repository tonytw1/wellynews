package nz.co.searchwellington.repositories.mongo

import java.util.UUID

import nz.co.searchwellington.model.{FeedAcceptancePolicy, Newsitem, Tag}
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test

import scala.concurrent.Await
import scala.concurrent.duration._

class MongoRepositoryTest {

  val TenSeconds = Duration(10, SECONDS)

  val mongoRepository = new MongoRepository("mongodb://localhost:27017/wellynews")

  @Test
  def canConnectToMongoAndReadTags {
    val tags = Await.result(mongoRepository.getAllTags(), TenSeconds)
    assertEquals(306, tags.size)
  }

  @Test
  def canReadTagGeocode = {
    val tagWithGeocode = Await.result(mongoRepository.getTagByUrlWords("arovalley"), TenSeconds).get
    assertTrue(tagWithGeocode.geocode_id.nonEmpty)
    assertTrue(tagWithGeocode.geocode.nonEmpty)
  }

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
  def canUpdateResources = {
    val title = "Test " + UUID.randomUUID.toString
    val newsitem = Newsitem(title = Some(title))
    mongoRepository.saveResource(newsitem)
    val updatedTitle = title + " updated"
    val updated = newsitem.copy(title = Some(updatedTitle), http_status = 200)

    Await.result(mongoRepository.saveResource(updated), TenSeconds)

    val reloaded = Await.result(mongoRepository.getResourceByObjectId(newsitem._id), TenSeconds).get
    assertEquals(updatedTitle, reloaded.title.get)
    assertEquals(200, reloaded.http_status)
  }

  @Test
  def canReadNewsitemPublisher = {
    val newsitemWithPublisher = Await.result(mongoRepository.getResourceByUrl("http://wellington.govt.nz/your-council/news/2016/11/wellington-quakes-21"), TenSeconds).get
    assertTrue(newsitemWithPublisher.asInstanceOf[Newsitem].publisher.nonEmpty)
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
  def feedAcceptancePolicyCanBeMappedToAnEnum: Unit = {
    val feeds = Await.result(mongoRepository.getAllFeeds(), TenSeconds)
    val feed = feeds.head

    assertEquals(FeedAcceptancePolicy.ACCEPT, feed.acceptance)
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
  def canReadUsers = {
    val users = Await.result(mongoRepository.getAllUsers, TenSeconds)
    assertTrue(users.nonEmpty)
  }

  @Test
  def canFindUsersByLinkedTwitterId = {
    val user = Await.result(mongoRepository.getUserByTwitterId(14497864), TenSeconds)
    assertTrue(user.nonEmpty)
  }

}
