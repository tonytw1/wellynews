package nz.co.searchwellington.controllers.admin

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import nz.co.searchwellington.controllers.{CommonModelObjectsService, LoggedInUserFilter}
import nz.co.searchwellington.filters.AdminRequestFilter
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.{Tag, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.elasticsearch.KeywordSearchService
import nz.co.searchwellington.repositories.{HandTaggingDAO, HibernateResourceDAO}
import nz.co.searchwellington.tagging.ImpliedTagService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView

@Controller class AutoTagController @Autowired()(var resourceDAO: HibernateResourceDAO, var requestFilter: AdminRequestFilter, var autoTagService: ImpliedTagService, var keywordSearchService: KeywordSearchService, var contentUpateService: ContentUpdateService, var tagVoteDAO: HandTaggingDAO, var loggedInUserFilter: LoggedInUserFilter, var commonModelObjectsService: CommonModelObjectsService) {

  private val log = Logger.getLogger(classOf[AutoTagController])

  @RequestMapping(Array("/*/autotag")) def prompt(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {

    val loggedInUser: User = loggedInUserFilter.getLoggedInUser
    if (loggedInUser == null) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN)
      null

    } else {
      val mv = new ModelAndView
      mv.setViewName("autoTagPrompt")
      commonModelObjectsService.populateCommonLocal(mv)
      mv.addObject("heading", "Autotagging")
      requestFilter.loadAttributesOntoRequest(request)

      val tag = request.getAttribute("tag").asInstanceOf[Tag]
      if (tag == null) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND)
        null
      } else {
        mv.addObject("tag", tag)
        mv.addObject("resources_to_tag", getPossibleAutotagResources(loggedInUserFilter.getLoggedInUser, tag))
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
          resourceDAO.loadResourceById(resourceId).map { resource =>
            log.info("Applying tag " + tag.getName + " to:" + resource.title)
            if (!autoTagService.alreadyHasTag(resource, tag)) {
              tagVoteDAO.addTag(loggedInUser, tag, resource)
            }
            contentUpateService.update(resource)
            resource
          }
        }

        mv.addObject("resources_to_tag", resourcesAutoTagged)
        mv
      }
    }
  }

  private def getPossibleAutotagResources(user: User, tag: Tag): Seq[FrontendResource] = {
    import scala.collection.JavaConversions._
    return keywordSearchService.getResourcesMatchingKeywordsNotTaggedByUser(tag.getDisplayName, true, user, tag)
  }

}
