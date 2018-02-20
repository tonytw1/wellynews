package nz.co.searchwellington.repositories.mongo

import org.junit.Assert.assertEquals
import org.junit.Test

import scala.concurrent.Await
import scala.concurrent.duration._

class MongoRepositoryTest {

  val mongoRepository = new MongoRepository("mongodb://localhost:27017/wellynews")

  @Test
  def canConnectToMongoAndReadTags {
    val tags = mongoRepository.getAllTags()
    assertEquals(306, tags.size)
  }

  @Test
  def canReadTagParent: Unit = {
    val tagWithParent = mongoRepository.getTagByName("vuw").get
    assertEquals(Some(28L), tagWithParent.parent)
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
  def canConnectToMongoAndReadFeeds {
    val feeds = mongoRepository.getAllFeeds()

    feeds.map { f =>
      println(f.id + " " + f.publisher)
    }

    assertEquals(414, feeds.size)

    println(Await.result(mongoRepository.getResourceById(54011), Duration(1, MINUTES)))
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
  def canReadResourcesByIdWithCorrectType = {
    val newsitems = mongoRepository.getAllNewsitems()
    val newsitem = newsitems.head
    val reloadedNewsitem = Await.result(mongoRepository.getResourceById(newsitem.id), Duration(1, MINUTES)).get

    val websites = mongoRepository.getAllWebsites()
    val website = websites.head
    val reloadedWebsite = Await.result(mongoRepository.getResourceById(website.id), Duration(1, MINUTES)).get
  }

  @Test
  def canReadResourceByUrl = {
    val newsitems = mongoRepository.getAllNewsitems()
    val newsitem = newsitems.head

    val reloadedByUrl = Await.result(mongoRepository.getResourceByUrl(newsitem.getUrl), Duration(1, MINUTES)).get

    assertEquals(newsitem, reloadedByUrl)
  }

}
