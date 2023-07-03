package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.IndexTagsService
import nz.co.searchwellington.urls.UrlParser
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.{ExecutionContext, Future}

@Component class ElasticSearchIndexRebuildService @Autowired()(mongoRepository: MongoRepository,
                                                               elasticSearchIndexer: ElasticSearchIndexer,
                                                               val indexTagsService: IndexTagsService,
                                                               val urlParser: UrlParser) extends IndexableResource
  with ReasonableWaits {

  private val log = LogFactory.getLog(classOf[ElasticSearchIndexRebuildService])

  private val BATCH_COMMIT_SIZE = 100

  protected[elasticsearch] def reindexResources(resourcesToIndex: Seq[BSONObjectID], i: Int = 0, totalResources: Int)(implicit ec: ExecutionContext): Future[Int] = {
    val remaining = resourcesToIndex.size

    def indexBatch(batch: Seq[BSONObjectID], i: Int): Future[Int] = {
      log.debug("Processing batch: " + batch.size + " - " + i + " / " + remaining)

      val eventualResources = Future.sequence(batch.map(i => mongoRepository.getResourceByObjectId(i))).map(_.flatten)
      val eventualWithIndexTags = eventualResources.flatMap { rs =>
        Future.sequence(rs.map(toIndexable))
      }

      eventualWithIndexTags.flatMap { rs =>
        elasticSearchIndexer.updateMultipleContentItems(rs).map { r =>
          if (r.isSuccess) {
            r.result.successes.size
          } else {
            log.warn(s"Got Elastic failure with status: ${r.status}")
            0
          }
        }
      }
    }

    log.info("Reindexing " + remaining + " remaining in batches of " + BATCH_COMMIT_SIZE)
    val batches = resourcesToIndex.grouped(BATCH_COMMIT_SIZE).toSeq

    batches.headOption.map { batch =>
      indexBatch(batch, i).flatMap { j =>
        val remaining = batches.tail
        if (remaining.nonEmpty) {
          reindexResources(remaining.flatten, j, totalResources)
        } else {
          Future.successful(j)
        }
      }

    }.getOrElse {
      Future.successful(i)
    }
  }

}
