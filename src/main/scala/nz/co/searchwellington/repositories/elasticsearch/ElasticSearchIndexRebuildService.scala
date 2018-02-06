package nz.co.searchwellington.repositories.elasticsearch

import com.fasterxml.jackson.core.JsonProcessingException
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class ElasticSearchIndexRebuildService @Autowired()(var mongoRepository: MongoRepository, val elasticSearchIndexer: ElasticSearchIndexer) {

  private val log = Logger.getLogger(classOf[ElasticSearchIndexRebuildService])
  private val BATCH_COMMIT_SIZE = 10

  private var running = false

  @throws[JsonProcessingException]
  def buildIndex(deleteAll: Boolean): Boolean = {
    /*if (running) {
      log.warn("The index builder is already running; cannot start another process")
      false

    } else {
      running = true
      try {
      */
    println("!!!!!!!!!! MEH")
        val resourceToIndex = mongoRepository.getAllWebsites()
        log.info("Number of resources to reindex: " + resourceToIndex.size)
        reindexResources(resourceToIndex)
        running = false
        true
    /*
      } catch {
        case e: Exception => {
          log.error("Unexpected error while reindexing", e)
        }
          running = false
          false
      }
      false
    }
    */
  }

  @throws[JsonProcessingException]
  private def reindexResources(resourceToIndex: Seq[Resource]) {
    println(resourceToIndex.size)
   // resourceToIndex.grouped(BATCH_COMMIT_SIZE).map { batch =>
    //  println("Processing batch: " + batch)
      reindexBatch(resourceToIndex)
    //}
    println("Index rebuild complete")
  }

  @throws[JsonProcessingException]
  private def reindexBatch(batch: Seq[Resource]) {
    //val resources = batch.map { id =>
    //  resourceDAO.loadResourceById(id)
    //}.flatten
    //import scala.collection.JavaConversions._
    println("Indexing batch: " + batch.size)
    elasticSearchIndexer.updateMultipleContentItems(batch)
  }

}
