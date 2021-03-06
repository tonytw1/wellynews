package nz.co.searchwellington.repositories

import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexRebuildService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class FrontendContentUpdater @Autowired()(elasticSearchIndexRebuildService: ElasticSearchIndexRebuildService) {

  private val log = Logger.getLogger(classOf[FrontendContentUpdater])

  def update(updatedResource: Resource)(implicit ec: ExecutionContext): Future[Boolean] = {
    log.debug("Updating elastic search record for resource: " + updatedResource.title)
    elasticSearchIndexRebuildService.index(updatedResource)
  }

}
