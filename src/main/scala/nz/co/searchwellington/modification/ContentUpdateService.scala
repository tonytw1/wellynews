package nz.co.searchwellington.modification

import nz.co.searchwellington.linkchecking.LinkCheckRequest
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.queues.{ElasticIndexQueue, LinkCheckerQueue}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class ContentUpdateService @Autowired()(mongoRepository: MongoRepository,
                                                   elasticIndexQueue: ElasticIndexQueue,
                                                   linkCheckerQueue: LinkCheckerQueue) {

  private val log = LogFactory.getLog(classOf[ContentUpdateService])

  def update(resource: Resource)(implicit ec: ExecutionContext): Future[Boolean] = {
    val eventualUrlHasChanged: Future[Boolean] = mongoRepository.getResourceByObjectId(resource._id).map { maybeExistingResource =>
      maybeExistingResource.forall { existingResource =>
        resource.page != existingResource.page
      }
    }

    eventualUrlHasChanged.flatMap { urlHasChanged =>
      mongoRepository.saveResource(resource).map { _ =>
        if (urlHasChanged) {
          queueLinkCheck(resource)
        }
        elasticIndexQueue.add(resource)
        true
      }
    }
  }

  def create(resource: Resource)(implicit ec: ExecutionContext): Future[Boolean] = {
    log.debug("Creating resource: " + resource.page)
    mongoRepository.saveResource(resource).map { r =>
      log.debug("Result of save for " + resource._id + " " + resource.page + ": " + r)
      if (r.writeErrors.isEmpty) {
        queueLinkCheck(resource)
        elasticIndexQueue.add(resource)
        true
      } else {
        false
      }
    }
  }

  private def queueLinkCheck(resource: Resource): Unit = {
    linkCheckerQueue.add(LinkCheckRequest(resource._id.stringify, resource.last_scanned))
  }
}
