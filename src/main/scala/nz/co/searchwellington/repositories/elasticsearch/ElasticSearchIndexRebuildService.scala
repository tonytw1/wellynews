package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.bson.BSONObjectID

import scala.concurrent.{ExecutionContext, Future}

@Component class ElasticSearchIndexRebuildService @Autowired()(mongoRepository: MongoRepository, elasticSearchIndexer: ElasticSearchIndexer,
                                                               taggingReturnsOfficerService: TaggingReturnsOfficerService) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[ElasticSearchIndexRebuildService])

  private val BATCH_COMMIT_SIZE = 1000

  def buildIndex()(implicit ec: ExecutionContext): Unit = {
    import scala.concurrent.Await
    val resourcesToIndex = Await.result(mongoRepository.getAllResourceIds(), OneMinute)
    Await.result(reindexResources(resourcesToIndex), OneMinute)
  }

  def index(resource: Resource)(implicit ec: ExecutionContext): Future[Int] = {
    reindexResources(Seq(resource._id))
  }

  private def reindexResources(resourcesToIndex: Seq[BSONObjectID], i: Int = 0)(implicit ec: ExecutionContext): Future[Int] = {

    def indexBatch(batch: Seq[BSONObjectID], i: Int): Future[Int] = {
      log.debug("Processing batch: " + batch.size + " - " + i + " / " + resourcesToIndex.size)
      val start = DateTime.now

      val eventualResources = Future.sequence(batch.map(i => mongoRepository.getResourceByObjectId(i))).map(_.flatten)
      val eventualWithIndexTags = eventualResources.flatMap { rs =>
        Future.sequence(rs.map { r =>
          getIndexTagIdsFor(r).map { tagIds =>
            (r, tagIds)
          }
        })
      }

      eventualWithIndexTags.flatMap { rs =>
        log.debug("Submitting batch for indexing")
        elasticSearchIndexer.updateMultipleContentItems(rs).map { r =>
          i + r.result.successes.size
        }
      }
    }

    log.debug("Reindexing: " + resourcesToIndex.size + " in batches of " + BATCH_COMMIT_SIZE)
    val batches= resourcesToIndex.grouped(BATCH_COMMIT_SIZE).toSeq

    batches.headOption.map { batch =>
      indexBatch(batch, i).flatMap { j =>
        val remaining = batches.tail
        if (remaining.nonEmpty) {
          reindexResources(remaining.flatten, j)
        } else {
          Future.successful(j)
        }
      }

    }.getOrElse {
      Future.successful(i)
    }
  }

  private def getIndexTagIdsFor(resource: Resource): Future[Seq[String]] = {
    val tags = taggingReturnsOfficerService.getIndexTagsForResource(resource)
    Future.successful(tags.map(_._id.stringify))
  }

}
