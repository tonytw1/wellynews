package nz.co.searchwellington.repositories.elasticsearch

import com.fasterxml.jackson.core.JsonProcessingException
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, MINUTES}
import scala.concurrent.{Await, Future}

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
      log.info("Processing batch: " + batch.size)

      val eventualResources = Future.sequence(batch.map(i => mongoRepository.getResourceById(i))).map(_.flatten)

      val eventualWithTags = eventualResources.flatMap { rs =>
        Future.sequence(rs.map { r =>
          getTagIdsFor(r).map { tagIds =>
            (r, tagIds)
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

  private def getTagIdsFor(resource: Resource) = {
    var taggingsFor: Future[Seq[mongoRepository.Tagging]] = mongoRepository.getTaggingsFor(resource.id)
    taggingsFor.map { ts =>
      val tagIds = ts.map(_.tag_id).toSet
      tagIds
    }
  }

}
