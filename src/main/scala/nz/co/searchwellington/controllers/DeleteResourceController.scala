package nz.co.searchwellington.controllers

import nz.co.searchwellington.filters.AdminRequestFilter
import nz.co.searchwellington.model._
import nz.co.searchwellington.modification.ContentDeletionService
import nz.co.searchwellington.permissions.EditPermissionService
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global

@Controller class DeleteResourceController @Autowired()(adminRequestFilter: AdminRequestFilter,
                                                        val loggedInUserFilter: LoggedInUserFilter, editPermissionService: EditPermissionService, urlStack: UrlStack,
                                                        contentDeletionService: ContentDeletionService, anonUserService: AnonUserService) extends RequiringLoggedInUser {

  private val log = LogFactory.getLog(classOf[DeleteResourceController])

  @RequestMapping(Array("/delete")) def delete(request: HttpServletRequest): ModelAndView = {
    def delete(loggedInUser: User): ModelAndView = {
      adminRequestFilter.loadAttributesOntoRequest(request)
      val editResource = request.getAttribute("resource").asInstanceOf[Resource]
      log.info("Resource to delete is: " + editResource)
      val canDelete = editResource != null && editPermissionService.canDelete(editResource, Some(loggedInUser))
      if (canDelete) {
        contentDeletionService.performDelete(editResource)
        editResource.`type` match {
          case "F" =>
            urlStack.setUrlStack(request, "")
          case _ =>
        }
      } else {
        log.warn("Not deleting resource: " + editResource)
      }
      new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)))
    }

    requiringAdminUser(delete)
  }

}
