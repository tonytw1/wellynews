package nz.co.searchwellington.repositories

import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexUpdateService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@deprecated
@Component class FrontendContentUpdater @Autowired()(elasticSearchIndexUpdateService: ElasticSearchIndexUpdateService) {

  private val log = Logger.getLogger(classOf[FrontendContentUpdater])

  def update(updatedResource: Resource): Unit = {
    log.info("Updating elastic search record for resource: " + updatedResource.title)
    // elasticSearchIndexUpdateService.updateSingleContentItem(updatedResource);
  }

}
