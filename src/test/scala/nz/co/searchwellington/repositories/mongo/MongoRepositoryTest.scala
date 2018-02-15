package nz.co.searchwellington.repositories.mongo

import nz.co.searchwellington.model.{Newsitem, Resource, Website, WebsiteImpl}
import org.junit.Assert.assertEquals
import org.junit.Test

import scala.concurrent.Await
import scala.concurrent.duration._

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

  @Test
  def canReadResourcesBackWithCorrectType = {
    val newsitems = mongoRepository.getAllNewsitems()
    val newsitem = newsitems.head
    val reloadedNewsitem = Await.result(mongoRepository.getResourceById(newsitem.id), Duration(1, MINUTES)).get

    val websites = mongoRepository.getAllWebsites()
    val website = websites.head
    val reloadedWebsite = Await.result(mongoRepository.getResourceById(website.id), Duration(1, MINUTES)).get
  }

}
