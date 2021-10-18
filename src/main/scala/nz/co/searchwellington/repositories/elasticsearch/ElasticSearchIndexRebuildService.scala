package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.IndexTagsService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.{ExecutionContext, Future}

@Component class ElasticSearchIndexRebuildService @Autowired()(mongoRepository: MongoRepository,
                                                               elasticSearchIndexer: ElasticSearchIndexer,
                                                               indexTagsService: IndexTagsService) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[ElasticSearchIndexRebuildService])

  private val BATCH_COMMIT_SIZE = 100

  def index(resource: Resource)(implicit ec: ExecutionContext): Future[Boolean] = {
    withTags(resource).map { toIndex =>
      elasticSearchIndexer.updateMultipleContentItems(Seq(toIndex))
    }.map { r =>
      r.isCompleted
    }
  }

  def reindexResources(resourcesToIndex: Seq[BSONObjectID], i: Int = 0)(implicit ec: ExecutionContext): Future[Int] = {

    def indexBatch(batch: Seq[BSONObjectID], i: Int): Future[Int] = {
      log.info("Processing batch: " + batch.size + " - " + i + " / " + resourcesToIndex.size)

      val eventualResources = Future.sequence(batch.map(i => mongoRepository.getResourceByObjectId(i))).map(_.flatten)
      val eventualWithIndexTags = eventualResources.flatMap { rs =>
        Future.sequence(rs.map(withTags))
      }

      eventualWithIndexTags.flatMap { rs =>
        log.debug("Submitting batch for indexing")
        elasticSearchIndexer.updateMultipleContentItems(rs).map { r =>
          i + r.result.successes.size
        }
      }
    }

    log.info("Reindexing: " + resourcesToIndex.size + " in batches of " + BATCH_COMMIT_SIZE)
    val batches = resourcesToIndex.grouped(BATCH_COMMIT_SIZE).toSeq

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

  private def withTags(resource: Resource)(implicit ec: ExecutionContext): Future[(Resource, Seq[String])] = {
    // TODO this is a confusing overloading of resource.resource_tags; elastic indexer also calls out got geo taggings itself.
    // Push down to the Elastic indexer or introduce a POJO?
    getIndexTagIdsFor(resource).map { tagIds =>
      (resource, tagIds)
    }
  }

  private def getIndexTagIdsFor(resource: Resource)(implicit ec: ExecutionContext): Future[Seq[String]] = {
    indexTagsService.getIndexTagsForResource(resource).map { tags =>
      tags.map(_._id.stringify)
    }
  }

}
