package nz.co.searchwellington.controllers.admin

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.{AcceptFeedItemController, LoggedInUserFilter, RequiringLoggedInUser}
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexRebuildService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.ExecutionContext.Implicits.global

@Order(2)
@Controller class RebuildIndexController @Autowired()(mongoRepository: MongoRepository,
                                                      elasticSearchIndexRebuildService: ElasticSearchIndexRebuildService,
                                                      val loggedInUserFilter: LoggedInUserFilter,
                                                      adminUrlBuilder: AdminUrlBuilder) extends ReasonableWaits
  with RequiringLoggedInUser {

  private val log = LogFactory.getLog(classOf[AcceptFeedItemController])

  @RequestMapping(value = Array("/admin/rebuild-index"), method = Array(RequestMethod.GET))
  def prompt(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    def rebuild(loggedInUser: User): ModelAndView = {
      val eventualResult = mongoRepository.getAllResourceIds().flatMap { resourceIds =>
        elasticSearchIndexRebuildService.reindexResources(resourceIds)
      }.map { i =>
        log.info("Reindexed " + i + " resources.")
        i
      }

      eventualResult.map { i =>
        log.info("Completed reindex: " + i)
      }

      new ModelAndView(new RedirectView(adminUrlBuilder.adminPage()))
    }

    requiringAdminUser(rebuild)
  }

}

