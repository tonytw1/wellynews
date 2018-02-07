package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Test

class ElasticSearchIT {

  val mongoRepository = new MongoRepository()
  val elasticSearchIndexer = new ElasticSearchIndexer()

  @Test
  def canIndexResources {
    val rebuild = new ElasticSearchIndexRebuildService(mongoRepository, elasticSearchIndexer)
    rebuild.buildIndex(false)
  }

}
