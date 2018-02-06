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
    assertEquals(306, newsitems.size)
  }

  @Test
  def canConnectToMongoAndReadWebsites {
    val websites = mongoRepository.getAllWebsites()
    assertEquals(306, websites.size)
  }

}
