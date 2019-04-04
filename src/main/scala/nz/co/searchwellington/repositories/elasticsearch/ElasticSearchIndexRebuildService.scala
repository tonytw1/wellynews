package nz.co.searchwellington.repositories.elasticsearch

import com.fasterxml.jackson.core.JsonProcessingException
import nz.co.searchwellington.model.{Resource, Tag}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, MINUTES}
import scala.concurrent.{Await, Future}

@Component class ElasticSearchIndexRebuildService @Autowired()(mongoRepository: MongoRepository, elasticSearchIndexer: ElasticSearchIndexer,
                                                               taggingReturnsOfficerService: TaggingReturnsOfficerService) {

  private val log = Logger.getLogger(classOf[ElasticSearchIndexRebuildService])

  private val BATCH_COMMIT_SIZE = 1000

  @throws[JsonProcessingException]
  def buildIndex(deleteAll: Boolean): Unit = {
    val resourcesToIndex = Await.result(mongoRepository.getAllResourceIds(), Duration(1, MINUTES))
    reindexResources(resourcesToIndex)
  }

  def index(resource: Resource): Unit = {
    reindexResources(Seq(resource._id))
  }

  @throws[JsonProcessingException]
  private def reindexResources(resourcesToIndex: Seq[BSONObjectID]): Unit = {
    log.info("Reindexing: " + resourcesToIndex.size + " in batches of " + BATCH_COMMIT_SIZE)
    val batches = resourcesToIndex.grouped(BATCH_COMMIT_SIZE)

    var i = 0
    batches.foreach { batch =>
      log.info("Processing batch: " + batch.size + " - " + i + " / " + resourcesToIndex.size)
      val start = DateTime.now

      val eventualResources = Future.sequence(batch.map(i => mongoRepository.getResourceByObjectId(i))).map(_.flatten)
      val eventualWithIndexTags = eventualResources.flatMap { rs =>
        Future.sequence(rs.map { r =>
          getIndexTagIdsFor(r).map { tagIds =>
            (r, tagIds)
          }
        })
      }

      val eventualIndexing = eventualWithIndexTags.map { rs =>
        log.info("Submitting batch for indexing")
        elasticSearchIndexer.updateMultipleContentItems(rs)
        i = i + rs.size
      }
      Await.result(eventualIndexing, Duration(1, MINUTES))
    }

    log.info("Index rebuild complete")
  }

  private def getIndexTagIdsFor(resource: Resource): Future[Seq[String]] = {
    val tags = taggingReturnsOfficerService.getIndexTagsForResource(resource)
    Future.successful(tags.map(_._id.stringify))
  }

}
