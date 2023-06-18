package nz.co.searchwellington.modification

import nz.co.searchwellington.linkchecking.LinkCheckRequest
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexRebuildService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class ContentUpdateService @Autowired()(mongoRepository: MongoRepository,
                                                   elasticSearchIndexRebuildService: ElasticSearchIndexRebuildService,
                                                   linkCheckerQueue: LinkCheckerQueue) {

  private val log = LogFactory.getLog(classOf[ContentUpdateService])

  def update(resource: Resource)(implicit ec: ExecutionContext): Future[Boolean] = {
    val eventualUrlHasChanged: Future[Boolean] = mongoRepository.getResourceByObjectId(resource._id).map { maybeExistingResource =>
      maybeExistingResource.forall { existingResource =>
        resource.page != existingResource.page
      }
    }

    eventualUrlHasChanged.flatMap { urlHasChanged =>
      mongoRepository.saveResource(resource).flatMap { _ =>
        elasticSearchIndexRebuildService.index(resource).map { r =>
          if (urlHasChanged) {
            queueLinkCheck(resource)
          }
          r
        }
      }
    }
  }

  def create(resource: Resource)(implicit ec: ExecutionContext): Future[Boolean] = {
    log.debug("Creating resource: " + resource.page)
    mongoRepository.saveResource(resource).flatMap { r =>
      log.debug("Result of save for " + resource._id + " " + resource.page + ": " + r)
      if (r.writeErrors.isEmpty) {
        elasticSearchIndexRebuildService.index(resource).map { _ =>
          queueLinkCheck(resource)
          true
        }
      } else {
        Future.successful(false)
      }
    }
  }

  private def queueLinkCheck(resource: Resource): Unit = {
    linkCheckerQueue.add(resource)
  }
}
