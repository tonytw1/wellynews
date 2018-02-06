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

}
