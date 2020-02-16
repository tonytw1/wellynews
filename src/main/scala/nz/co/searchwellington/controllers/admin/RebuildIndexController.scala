package nz.co.searchwellington.controllers.admin

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.controllers.AcceptFeedItemController
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexRebuildService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global

@Order(2)
@Controller class RebuildIndexController @Autowired()(mongoRepository: MongoRepository, elasticSearchIndexRebuildService: ElasticSearchIndexRebuildService) {
  private val log = Logger.getLogger(classOf[AcceptFeedItemController])

  @RequestMapping(value = Array("/admin/rebuild-index"), method = Array(RequestMethod.GET)) def prompt(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    mongoRepository.getAllResourceIds().flatMap { resourceIds =>
      elasticSearchIndexRebuildService.reindexResources(resourceIds)
    }.map { i =>
      log.info("Reindexed " + i + " resources.")
    }

    new ModelAndView("TODO")
  }

}
