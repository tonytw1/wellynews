package nz.co.searchwellington.repositories.elasticsearch

import com.fasterxml.jackson.core.JsonProcessingException
import nz.co.searchwellington.repositories.HibernateResourceDAO
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class ElasticSearchIndexRebuildService @Autowired()(var resourceDAO: HibernateResourceDAO, val elasticSearchIndexUpdateService: ElasticSearchIndexUpdateService) {

  private val log = Logger.getLogger(classOf[ElasticSearchIndexRebuildService])
  private val BATCH_COMMIT_SIZE: Int = 10

  private var running = false

  @throws[JsonProcessingException]
  def buildIndex(deleteAll: Boolean): Boolean = {
    if (running) {
      log.warn("The index builder is already running; cannot start another process")
      false

    } else {
      running = true
      try {
        val resourceIdsToIndex = resourceDAO.getAllResourceIds
        log.info("Number of resources to reindex: " + resourceIdsToIndex.size)
        reindexResources(resourceIdsToIndex)
        running = false
        true
      } catch {
        case e: Exception => {
          log.error("Unexpected error while reindexing", e)
        }
          running = false
          false
      }
      false
    }
  }

  @throws[JsonProcessingException]
  private def reindexResources(resourceIdsToIndex: Seq[Integer]) {
    resourceIdsToIndex.grouped(BATCH_COMMIT_SIZE).map { batch =>
      log.info("Processing batch: " + batch)
      reindexBatch(batch)
    }
    log.info("Index rebuild complete")
  }

  @throws[JsonProcessingException]
  private def reindexBatch(batch: Seq[Integer]) {
    val resources = batch.map { id =>
      resourceDAO.loadResourceById(id)
    }.flatten
    import scala.collection.JavaConversions._
    elasticSearchIndexUpdateService.updateMultipleContentItems(resources)
  }

}
