package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Resource, User}
import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.views.Errors
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{PathVariable, RequestMapping}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import javax.servlet.http.HttpServletRequest
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller class CheckResourceController @Autowired()(queue: LinkCheckerQueue, urlStack: UrlStack,
                                                       val loggedInUserFilter: LoggedInUserFilter, mongoRepository: MongoRepository)
  extends RequiringLoggedInUser with ReasonableWaits with Errors {

  private val log = LogFactory.getLog(classOf[CheckResourceController])

  @RequestMapping(Array("/check-resource/{id}"))
  def addToQueue(@PathVariable id: String, request: HttpServletRequest): ModelAndView = {
    def checkResource(loggedInUser: User): ModelAndView = {
      getResourceById(id).map { resource =>
        log.info("Adding resource to queue: " + resource.id + "(" + resource._id.stringify + ")")
        queue.add(resource._id.stringify)
        new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)))
      }
    }.getOrElse {
      NotFound
    }

    requiringAdminUser(checkResource)
  }

  private def getResourceById(id: String): Option[Resource] = {
    Await.result(mongoRepository.getResourceById(id), TenSeconds)
  }

}
