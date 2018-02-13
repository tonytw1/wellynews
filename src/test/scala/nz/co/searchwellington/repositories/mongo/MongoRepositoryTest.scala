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

  def canReadResourceTagsFromMongo = {
    val taggings = mongoRepository.getAllTaggings()
    assertEquals(2, taggings)
  }

}
