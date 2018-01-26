package nz.co.searchwellington.repositories.elasticsearch

import com.fasterxml.jackson.core.JsonProcessingException
import nz.co.searchwellington.repositories.HibernateResourceDAO
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class ElasticSearchIndexRebuildService @Autowired()(var resourceDAO: HibernateResourceDAO, val elasticSearchIndexUpdateService: ElasticSearchIndexUpdateService) {

  private val log: Logger = Logger.getLogger(classOf[ElasticSearchIndexRebuildService])
  private val BATCH_COMMIT_SIZE: Int = 10

  this.running = false
  private var running: Boolean = false

  @throws[JsonProcessingException]
  def buildIndex(deleteAll: Boolean): Boolean = {
    if (running) {
      log.warn("The index builder is already running; cannot start another process")
      return false
    }
    running = true
    try {
      val resourceIdsToIndex = resourceDAO.getAllResourceIds
      log.info("Number of resources to reindex: " + resourceIdsToIndex.size)
      if (resourceIdsToIndex.size > 0) {
        reindexResources(resourceIdsToIndex)
        running = false
        return true
      }
    }
    catch {
      case e: Exception => {
        log.error("Unexpected error while reindexing", e)
      }
    }
    running = false
    return false
  }

  @throws[JsonProcessingException]
  private def reindexResources(resourceIdsToIndex: Seq[Integer]) {
    var batchCounter: Int = 0
    val all = resourceIdsToIndex
    while (all.size > batchCounter + BATCH_COMMIT_SIZE) {
      {
        val batch: java.util.List[Integer] = all.subList(batchCounter, batchCounter + BATCH_COMMIT_SIZE)
        log.info("Processing batch starting at " + batchCounter + " / " + all.size)
        reindexBatch(batch)
        batchCounter = batchCounter + BATCH_COMMIT_SIZE
      }
    }

    val batch = all.subList(batchCounter, all.size - 1)
    reindexBatch(batch)
    log.info("Index rebuild complete")
  }

  @throws[JsonProcessingException]
  private def reindexBatch(batch: Seq[Integer]) {
    val resources = batch.map { id =>
      resourceDAO.loadResourceById(id)
    }
    import scala.collection.JavaConversions._
    elasticSearchIndexUpdateService.updateMultipleContentItems(resources)
  }
}