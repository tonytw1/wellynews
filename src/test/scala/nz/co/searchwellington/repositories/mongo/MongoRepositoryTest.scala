package nz.co.searchwellington.repositories.mongo

import org.junit.Assert.assertEquals
import org.junit.Test

import scala.concurrent.Await
import scala.concurrent.duration._

class MongoRepositoryTest {

  val mongoRepository = new MongoRepository("mongodb://localhost:27017/wellynews")

  @Test
  def canConnectToMongoAndReadTags {
    val tags = Await.result(mongoRepository.getAllTags(), Duration(10, SECONDS))
    assertEquals(306, tags.size)
  }

  @Test
  def canReadTagParent = {
    val tagWithParent = Await.result(mongoRepository.getTagByName("vuw"), Duration(10, SECONDS)).get
    assertEquals(Some(28L), tagWithParent.parent)
  }

  @Test
  def canConnectToMongoAndReadFeeds {
    val feeds = Await.result(mongoRepository.getAllFeeds(), Duration(10, SECONDS))
    assertEquals(414, feeds.size)
  }

  @Test
  def canReadResourceTagsFromMongo = {
    val taggings = Await.result(mongoRepository.getAllTaggings(), Duration(10, SECONDS))
    assertEquals(30435, taggings.size)
  }

  @Test
  def canReadTaggingsForResource = {
    val taggings = Await.result(mongoRepository.getTaggingsFor(6833), Duration(1, MINUTES))
    println(taggings)
    assertEquals(1, taggings.size)
    assertEquals(8, taggings.head.tag_id)
  }

  @Test
  def canReadListOfResourceIds = {
    val resourceIds = Await.result(mongoRepository.getAllResourceIds(), Duration(1, MINUTES))
    assertEquals(73049, resourceIds.size)
  }

}
