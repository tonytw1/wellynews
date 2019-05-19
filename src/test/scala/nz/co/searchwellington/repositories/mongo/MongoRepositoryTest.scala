package nz.co.searchwellington.repositories.mongo

import java.util.UUID

import nz.co.searchwellington.model.{FeedAcceptancePolicy, Newsitem}
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test

import scala.concurrent.Await
import scala.concurrent.duration._

class MongoRepositoryTest {

  val TenSeconds = Duration(10, SECONDS)

  val mongoRepository = new MongoRepository("mongodb://localhost:27017/searchwellington")

  @Test
  def canConnectToMongoAndReadTags {
    val tags = Await.result(mongoRepository.getAllTags(), TenSeconds)
    assertEquals(306, tags.size)
  }

  @Test
  def canReadTagParent = {
    val tagWithParent = Await.result(mongoRepository.getTagByUrlWords("vuw"), TenSeconds).get
    val parentId = tagWithParent.parent.get
    val parentTag = Await.result(mongoRepository.getTagByObjectId(parentId), TenSeconds).get
    assertEquals("education", parentTag.name)
  }

  @Test
  def canReadTagGeocode = {
    val tagWithGeocode = Await.result(mongoRepository.getTagByUrlWords("arovalley"), TenSeconds).get
    assertTrue(tagWithGeocode.geocode_id.nonEmpty)
    assertTrue(tagWithGeocode.geocode.nonEmpty)
  }

  @Test
  def canPersistResources = {
    val title = "Test " + UUID.randomUUID.toString
    val newsitem = Newsitem(title = Some(title))

    Await.result(mongoRepository.saveResource(newsitem), TenSeconds)

    val reloaded = Await.result(mongoRepository.getResourceByObjectId(newsitem._id), TenSeconds)
    assertTrue(reloaded.nonEmpty)
    assertEquals(title, reloaded.get.title.get)
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
