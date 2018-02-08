package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Test

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
  def canReadResources: Unit = {
    elasticSearchIndexer.readBack()
  }

}
