package nz.co.searchwellington.controllers

import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.linkchecking.LinkCheckRequest
import nz.co.searchwellington.model.{Resource, User}
import nz.co.searchwellington.modification.ContentDeletionService
import nz.co.searchwellington.permissions.EditPermissionService
import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.views.Errors
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{PathVariable, RequestMapping}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller class ManageResourceController @Autowired()(queue: LinkCheckerQueue, urlStack: UrlStack,
                                                        val loggedInUserFilter: LoggedInUserFilter,
                                                        mongoRepository: MongoRepository,
                                                        editPermissionService: EditPermissionService,
                                                        contentDeletionService: ContentDeletionService)
  extends RequiringLoggedInUser with ReasonableWaits with Errors {

  private val log = LogFactory.getLog(classOf[ManageResourceController])

  @RequestMapping(Array("/check-resource/{id}"))
  def check(@PathVariable id: String, request: HttpServletRequest): ModelAndView = {
    def checkResource(loggedInUser: User): ModelAndView = {
      getResourceById(id).map { resource =>
        log.info("Adding resource to queue: " + resource.id + "(" + resource._id.stringify + ")")
        queue.add(resource)
        new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)))
      }
    }.getOrElse {
      NotFound
    }

    requiringAdminUser(checkResource)
  }

  @RequestMapping(Array("/delete-resource/{id}"))
  def delete(@PathVariable id: String, request: HttpServletRequest): ModelAndView = {
    def deleteResource(loggedInUser: User): ModelAndView = {
      getResourceById(id).map { resource =>
        log.info("Resource to delete is: " + resource)
        val canDelete = editPermissionService.canDelete(resource, Some(loggedInUser))
        if (canDelete) {
          contentDeletionService.performDelete(resource)
          resource.`type` match {
            case "F" =>
              urlStack.setUrlStack(request, "")
            case _ =>
          }
          new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)))

        } else {
          log.warn("Not allowed to delete resource: " + resource)
          NotAllowed
        }
      }.getOrElse{
        NotFound
      }
    }

    requiringAdminUser(deleteResource)  // TODO let normal users delete their own resources as per above
  }

  private def getResourceById(id: String): Option[Resource] = {
    Await.result(mongoRepository.getResourceById(id), TenSeconds)
  }

}
