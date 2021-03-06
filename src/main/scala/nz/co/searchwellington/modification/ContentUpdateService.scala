package nz.co.searchwellington.modification

import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.repositories.FrontendContentUpdater
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class ContentUpdateService @Autowired()(mongoRepository: MongoRepository,
                                                   frontendContentUpdater: FrontendContentUpdater,
                                                   linkCheckerQueue: LinkCheckerQueue) {

  private val log = Logger.getLogger(classOf[ContentUpdateService])

  def update(resource: Resource)(implicit ec: ExecutionContext): Future[Boolean] = {
    log.debug("Updating content for: " + resource.title + " - " + resource.http_status + " " + resource.page)
    try {
      /*
      var resourceUrlHasChanged = false
      val newSubmission = resource._id.isEmpty
      if (!newSubmission) {
        mongoRepository.getResourceByObjectId(resource._id.get).map { maybeExistingResource =>
          maybeExistingResource.map { existingResource =>
            resourceUrlHasChanged = resource.page.flatMap { rp =>
              existingResource.page.map { ep =>
                rp != ep
              }
            }.getOrElse(false)
          }
        }
      }

      if (newSubmission || resourceUrlHasChanged) {
        resource.setHttpStatus(0)
      }
      */

      mongoRepository.saveResource(resource).flatMap { _ =>
        frontendContentUpdater.update(resource)
      }
    }
    catch {
      case e: Exception =>
        log.error("Error: ", e)
        Future.failed(e)
    }
  }

  def create(resource: Resource)(implicit ec: ExecutionContext): Future[Resource] = {
    resource.setHttpStatus(0)
    log.debug("Creating resource: " + resource.page)
    mongoRepository.saveResource(resource).flatMap { r =>
      log.debug("Result of save for " + resource._id + " " + resource.page + ": " + r)
      frontendContentUpdater.update(resource).map { _ =>
        linkCheckerQueue.add(resource._id.stringify)
        resource
      }
    }
  }

}
