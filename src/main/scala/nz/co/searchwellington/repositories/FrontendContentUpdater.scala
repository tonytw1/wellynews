package nz.co.searchwellington.repositories

import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.repositories.elasticsearch.{ElasticSearchIndexRebuildService, ElasticSearchIndexUpdateService}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@deprecated
@Component class FrontendContentUpdater @Autowired()(elasticSearchIndexUpdateService: ElasticSearchIndexUpdateService,
                                                     elasticSearchIndexRebuildService: ElasticSearchIndexRebuildService) {

  private val log = Logger.getLogger(classOf[FrontendContentUpdater])

  def update(updatedResource: Resource): Unit = {
    log.info("Updating elastic search record for resource: " + updatedResource.title)
    elasticSearchIndexRebuildService.index(updatedResource)
    // elasticSearchIndexUpdateService.updateSingleContentItem(updatedResource);
  }

}
