package nz.co.searchwellington.modification

import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.repositories.{FrontendContentUpdater, HibernateResourceDAO}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component class ContentUpdateService @Autowired() (resourceDAO: HibernateResourceDAO, linkCheckerQueue: LinkCheckerQueue, frontendContentUpdater: FrontendContentUpdater) {

  private val log = Logger.getLogger(classOf[ContentUpdateService])

  def update(resource: Resource) {
    log.info("Updating content for: " + resource.title + " - " + resource.page)
    try {
      var resourceUrlHasChanged = false
      val newSubmission = resource.id == 0
      if (!newSubmission) {
        resourceDAO.loadResourceById(resource.id).map { existingResource =>
          resourceUrlHasChanged = !(resource.getUrl == existingResource.getUrl)
        }
      }
      if (newSubmission || resourceUrlHasChanged) {
        resource.setHttpStatus(0)
      }
      resourceDAO.saveResource(resource)
      frontendContentUpdater.update(resource)
    }
    catch {
      case e: Exception => {
        log.error("Error: ", e)
      }
    }
  }

  def create(resource: Resource) {
    resource.setHttpStatus(0)
    resourceDAO.saveResource(resource)
    linkCheckerQueue.add(resource.id)
  }

}
