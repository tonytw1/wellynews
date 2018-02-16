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
    val newsitems = Await.result(elasticSearchIndexer.getResources(ResourceQuery(`type` = Some("N"))), Duration(10, SECONDS))
    assertTrue(newsitems._1.nonEmpty)
    assertTrue(newsitems._1.forall(i => Await.result(mongoRepository.getResourceById(i), Duration(1, MINUTES)).get.`type` == "N"))

    val websites = Await.result(elasticSearchIndexer.getResources(ResourceQuery(`type` = Some("W"))), Duration(10, SECONDS))
    assertTrue(websites._1.nonEmpty)
    assertTrue(websites._1.forall(i => Await.result(mongoRepository.getResourceById(i), Duration(10, SECONDS)).get.`type` == "W"))
  }

  @Test
  def canFilterByTag: Unit = {
    // TODO implement
  }

}
