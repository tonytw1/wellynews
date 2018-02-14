package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Test
import org.junit.Assert.assertTrue

import scala.concurrent.Await
import scala.concurrent.duration._

class ElasticSearchIT {

  val mongoRepository = new MongoRepository()
  val elasticSearchIndexer = new ElasticSearchIndexer()

  val rebuild = new ElasticSearchIndexRebuildService(mongoRepository, elasticSearchIndexer)

  @Test
  def canCreateIndexes: Unit = {
    elasticSearchIndexer.createIndexes()
  }

  @Test
  def canIndexResources {
    rebuild.buildIndex(false)
  }

  @Test
  def canFilterByType: Unit = {
    val newsitems = elasticSearchIndexer.getLatestNewsitems(10)

    assertTrue(newsitems.nonEmpty)
    assertTrue(newsitems.forall(i => Await.result(mongoRepository.getResourceById(i), Duration(1, MINUTES)).get.`type` == "N"))

    newsitems.map { n =>
      println(n)
    }

    /*val websites = elasticSearchIndexer.getLatestWebsites(10)
    assertTrue(websites.nonEmpty)
    assertTrue(websites.forall(i => mongoRepository.getResourceById(i).get.`type` == "W"))
    */
  }

}
