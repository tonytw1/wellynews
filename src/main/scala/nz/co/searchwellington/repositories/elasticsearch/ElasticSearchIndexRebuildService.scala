package nz.co.searchwellington.repositories.elasticsearch

import com.fasterxml.jackson.core.JsonProcessingException
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{Duration, MINUTES}

@Component class ElasticSearchIndexRebuildService @Autowired()(mongoRepository: MongoRepository, elasticSearchIndexer: ElasticSearchIndexer) {

  private val log = Logger.getLogger(classOf[ElasticSearchIndexRebuildService])
  private val BATCH_COMMIT_SIZE = 1000

  @throws[JsonProcessingException]
  def buildIndex(deleteAll: Boolean): Unit = {
    val resourcesToIndex = Await.result(mongoRepository.getAllResourceIds(), Duration(1, MINUTES))
    reindexResources(resourcesToIndex)


  }

  @throws[JsonProcessingException]
  private def reindexResources(resourcesToIndex: Seq[Int]): Unit = {
    val batches = resourcesToIndex.grouped(BATCH_COMMIT_SIZE)

    batches.foreach { batch =>
      println("Processing batch: " + batch.size)

      val eventualResources = Future.sequence(batch.map(i => mongoRepository.getResourceById(i))).map(_.flatten)

      val eventualWithTags = eventualResources.flatMap { rs =>
        Future.sequence(rs.map { r =>
          mongoRepository.getTaggingsFor(r.id).map { ts =>
            log.info("Tags: " + ts.size)
            (r, ts.map(_.tag_id).toSet)
          }
        })
      }

      val x: Future[Unit] = eventualWithTags.map { rs =>
        elasticSearchIndexer.updateMultipleContentItems(rs)
      }
      Await.result(x, Duration(1, MINUTES))
      println("Next")
    }

    println("Index rebuild complete")
  }

}
