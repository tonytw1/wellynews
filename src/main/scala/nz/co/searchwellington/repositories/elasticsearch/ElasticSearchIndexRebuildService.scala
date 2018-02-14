package nz.co.searchwellington.repositories.elasticsearch

import com.fasterxml.jackson.core.JsonProcessingException
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class ElasticSearchIndexRebuildService @Autowired()(var mongoRepository: MongoRepository, val elasticSearchIndexer: ElasticSearchIndexer) {

  private val log = Logger.getLogger(classOf[ElasticSearchIndexRebuildService])
  private val BATCH_COMMIT_SIZE = 100

  @throws[JsonProcessingException]
  def buildIndex(deleteAll: Boolean): Unit = {
    mongoRepository.getAllResourceIds().map { resourcesToIndex =>
      log.info("Number of resources to reindex: " + resourcesToIndex.size)
      reindexResources(resourcesToIndex)
    }
  }

  @throws[JsonProcessingException]
  private def reindexResources(resourcesToIndex: Seq[Int]) {
    val batches = resourcesToIndex.grouped(BATCH_COMMIT_SIZE)
    batches.foreach { batch =>
      println("Processing batch: " + batch.size)

      val eventualResources = Future.sequence(batch.map(i => mongoRepository.getResourceById(i))).map(_.flatten)

      val eventualWithTags = eventualResources.flatMap { rs =>
        Future.sequence(rs.map { r =>
          mongoRepository.getTaggingsFor(r.id).map { ts =>
            (r, ts.map(_.tag_id).toSet)
          }
        })
      }

      eventualWithTags.map { rs =>
        elasticSearchIndexer.updateMultipleContentItems(rs)
      }
    }

    println("Index rebuild complete")
  }

}
