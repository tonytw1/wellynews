package nz.co.searchwellington.repositories.mongo

import org.junit.Assert.assertEquals
import org.junit.Test

class MongoRepositoryTest {

  val mongoRepository = new MongoRepository()

  @Test
  def canConnectToMongoAndReadTags {
    val tags = mongoRepository.getAllTags()

    assertEquals(306, tags.size)
  }

  @Test
  def canConnectToMongoAndReadNewsitems {
    val newsitems = mongoRepository.getAllNewsitems()
    assertEquals(50813, newsitems.size)
  }

  @Test
  def canConnectToMongoAndReadWebsites {
    val websites = mongoRepository.getAllWebsites()
    assertEquals(21644, websites.size)
  }

  @Test
  def canReadResourceTagsFromMongo = {
    val taggings = mongoRepository.getAllTaggings()
    assertEquals(30435, taggings.size)
  }

  @Test
  def canReadTaggingsForResource = {
    val taggings = mongoRepository.getTaggingsFor(6833)
    println(taggings)
    assertEquals(1, taggings.size)
    assertEquals(8, taggings.head.tag_id)
  }

  def canReadListOfResourceIds = {
    val resourceIds: Seq[Int] = mongoRepository.getAllResourceIds()
    assertEquals(30435, resourceIds.size)
  }

}
