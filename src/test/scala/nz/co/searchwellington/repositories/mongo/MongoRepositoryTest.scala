package nz.co.searchwellington.repositories.mongo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
  def canReadTagParent = {
    val tagWithParent = Await.result(mongoRepository.getTagByName("vuw"), TenSeconds).get
    assertEquals(Some(28L), tagWithParent.parent)
  }

  @Test
  def canConnectToMongoAndReadFeeds {
    val feeds = Await.result(mongoRepository.getAllFeeds(), TenSeconds)
    assertEquals(414, feeds.size)
  }

  @Test
  def canReadResourceTagsFromMongo = {
    val taggings = Await.result(mongoRepository.getAllTaggings(), TenSeconds)
    assertEquals(30435, taggings.size)
  }

  @Test
  def canReadTaggingsForResource = {
    val taggings = Await.result(mongoRepository.getTaggingsFor(6833), TenSeconds)
    println(taggings)
    assertEquals(1, taggings.size)
    assertEquals(8, taggings.head.tag_id)
  }

  @Test
  def canReadListOfResourceIds = {
    val resourceIds = Await.result(mongoRepository.getAllResourceIds(), TenSeconds)
    assertEquals(73049, resourceIds.size)
  }

  @Test
  def canReadUsers = {
    val users = Await.result(mongoRepository.getAllUsers(), TenSeconds)
    println(users.size)
  }

  @Test
  def canFindUsersByLinkedTwitterId = {
    val user = Await.result(mongoRepository.getUserByTwitterId(14497864), TenSeconds)
    println("GOT: " + user)
    assertTrue(user.nonEmpty)
  }

}
