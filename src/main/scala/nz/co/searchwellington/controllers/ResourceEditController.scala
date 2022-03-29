package nz.co.searchwellington.controllers

import com.google.common.base.Strings
import nz.co.searchwellington.feeds.whakaoko.WhakaokoService
import nz.co.searchwellington.feeds.{FeedItemAcceptor, FeeditemToNewsitemService}
import nz.co.searchwellington.filters.AdminRequestFilter
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.modification.{ContentDeletionService, ContentUpdateService}
import nz.co.searchwellington.permissions.EditPermissionService
import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.repositories.{ContentRetrievalService, HandTaggingDAO}
import nz.co.searchwellington.spam.SpamFilter
import nz.co.searchwellington.tagging.AutoTaggingService
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import java.util.UUID
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller class ResourceEditController @Autowired()(adminRequestFilter: AdminRequestFilter,
                                                      autoTagger: AutoTaggingService,
                                                      val loggedInUserFilter: LoggedInUserFilter, editPermissionService: EditPermissionService, urlStack: UrlStack,
                                                      submissionProcessingService: SubmissionProcessingService, contentUpdateService: ContentUpdateService,
                                                      contentDeletionService: ContentDeletionService, anonUserService: AnonUserService,
                                                      tagVoteDAO: HandTaggingDAO, feedItemAcceptor: FeedItemAcceptor,
                                                      feednewsItemToNewsitemService: FeeditemToNewsitemService, urlWordsGenerator: UrlWordsGenerator,
                                                      whakaokoService: WhakaokoService, frontendResourceMapper: FrontendResourceMapper,
                                                      spamFilter: SpamFilter, linkCheckerQueue: LinkCheckerQueue,
                                                      val contentRetrievalService: ContentRetrievalService) extends CommonModelObjectsService with RequiringLoggedInUser {

  private val log = LogFactory.getLog(classOf[ResourceEditController])

  @RequestMapping(Array("/delete")) def delete(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
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
