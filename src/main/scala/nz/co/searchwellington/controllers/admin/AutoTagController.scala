package nz.co.searchwellington.controllers.admin

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.{CommonModelObjectsService, LoggedInUserFilter}
import nz.co.searchwellington.filters.AdminRequestFilter
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.{Tag, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.{ContentRetrievalService, HandTaggingService}
import nz.co.searchwellington.repositories.elasticsearch.KeywordSearchService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.ImpliedTagService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller class AutoTagController @Autowired()(mongoRepository: MongoRepository,
                                                 requestFilter: AdminRequestFilter,
                                                 autoTagService: ImpliedTagService,
                                                 keywordSearchService: KeywordSearchService,
                                                 contentUpdateService: ContentUpdateService,
                                                 loggedInUserFilter: LoggedInUserFilter,
                                                 commonModelObjectsService: CommonModelObjectsService,
                                                 handTaggingService: HandTaggingService,
                                                 contentRetrievalService: ContentRetrievalService) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[AutoTagController])

  @RequestMapping(Array("/*/autotag")) def prompt(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    Option(loggedInUserFilter.getLoggedInUser).fold {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN)
      null: ModelAndView

    } { loggedInUser =>
      Option(request.getAttribute("tag").asInstanceOf[Tag]).fold {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND)
        null: ModelAndView

      } { tag =>
        import scala.collection.JavaConverters._
        val mv = new ModelAndView("autoTagPrompt").
          addObject("heading", "Autotagging").
          addObject("tag", tag).
          addObject("resources_to_tag", getPossibleAutotagResources(loggedInUser, tag).asJava)

        commonModelObjectsService.populateCommonLocal(mv)
        mv
      }
    }
  }

  @RequestMapping(value = Array("/*/autotag/apply"), method = Array(RequestMethod.POST)) def apply(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val loggedInUser: User = loggedInUserFilter.getLoggedInUser
    if (loggedInUser == null) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN)
      null

    } else {
      requestFilter.loadAttributesOntoRequest(request)
      val tag = request.getAttribute("tag").asInstanceOf[Tag]
      if (tag == null) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND)
        null

      } else {
        val mv = new ModelAndView("autoTagApply")
        mv.addObject("heading", "Autotagging")
        mv.addObject("tag", tag)
        commonModelObjectsService.populateCommonLocal(mv)

        val autotaggedResourceIds = request.getParameterValues("autotag")

        val resourcesAutoTagged = autotaggedResourceIds.flatMap { resourceId =>
          Await.result(mongoRepository.getResourceById(resourceId), TenSeconds).map { resource =>
            log.info("Applying tag " + tag.getName + " to:" + resource.title)
            if (!autoTagService.alreadyHasTag(resource, tag)) {
              handTaggingService.addTag(loggedInUser, tag, resource)
            }
            contentUpdateService.update(resource)
            resource
          }
        }

        mv.addObject("resources_to_tag", resourcesAutoTagged)
        mv
      }
    }
  }

  private def getPossibleAutotagResources(user: User, tag: Tag): Seq[FrontendResource] = {
    val keywords = tag.getDisplayName // TODO OR with autotagging hints
    contentRetrievalService.getResourcesMatchingKeywordsNotTaggedByUser(keywords, user, tag)
  }

}
