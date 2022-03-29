package nz.co.searchwellington.controllers.admin

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.{CommonModelObjectsService, LoggedInUserFilter, UrlStack}
import nz.co.searchwellington.filters.AdminRequestFilter
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.modification.TagModificationService
import nz.co.searchwellington.permissions.EditPermissionService
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller class TagEditController @Autowired()(requestFilter: AdminRequestFilter,
                                                 urlStack: UrlStack, tagModifcationService: TagModificationService,
                                                 loggedInUserFilter: LoggedInUserFilter,
                                                 editPermissionService: EditPermissionService,
                                                 val contentRetrievalService: ContentRetrievalService)
  extends ReasonableWaits with CommonModelObjectsService {

  @GetMapping(Array("/edit/tag/delete"))
  def delete(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val loggedInUser = loggedInUserFilter.getLoggedInUser

    if (!editPermissionService.canDeleteTags(loggedInUser)) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN)
      return null
    }
    requestFilter.loadAttributesOntoRequest(request)
    if (request.getAttribute("tag") == null) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND)
      return null
    }

    val mv = new ModelAndView("deleteTag").
      addObject("heading", "Editing a Tag")

    val tag = request.getAttribute("tag").asInstanceOf[Tag]
    mv.addObject("tag", tag)
    tagModifcationService.deleteTag(tag)
    urlStack.setUrlStack(request, "")
    mv
  }

}
