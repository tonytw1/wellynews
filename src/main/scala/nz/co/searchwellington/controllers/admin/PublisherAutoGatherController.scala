package nz.co.searchwellington.controllers.admin

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.{CommonModelObjectsService, LoggedInUserFilter, RequiringLoggedInUser}
import nz.co.searchwellington.filters.AdminRequestFilter
import nz.co.searchwellington.model.{Newsitem, Resource, User, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlParser
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller class PublisherAutoGatherController @Autowired()(requestFilter: AdminRequestFilter,
                                                             mongoRepository: MongoRepository,
                                                             contentUpdateService: ContentUpdateService,
                                                             urlParser: UrlParser,
                                                             val contentRetrievalService: ContentRetrievalService,
                                                             val loggedInUserFilter: LoggedInUserFilter) extends
  ReasonableWaits with CommonModelObjectsService with RequiringLoggedInUser {

  private val log = Logger.getLogger(classOf[PublisherAutoGatherController])

  @RequestMapping(Array("/admin/gather/prompt")) def prompt(request: HttpServletRequest): ModelAndView = {
    def prompt(loggedInUser: User): ModelAndView = {
      val mv = new ModelAndView("autoGatherPrompt").
        addObject("heading", "Auto Gathering")

      requestFilter.loadAttributesOntoRequest(request)
      val publisher = request.getAttribute("publisher").asInstanceOf[Website]
      mv.addObject("publisher", publisher)
      if (publisher != null) {
        val resourcesToAutoTag = getPossibleAutotagResources(publisher).filter { resource =>
          needsPublisher(resource.asInstanceOf[Newsitem], publisher)
        }
        mv.addObject("resources_to_tag", resourcesToAutoTag)
      }

      Await.result(withCommonLocal(mv), TenSeconds)
    }

    requiringAdminUser(prompt)
  }

  @RequestMapping(value = Array("/admin/gather/apply"), method = Array(RequestMethod.POST)) def apply(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    def apply(loggedInUser: User): ModelAndView = {
      val mv = new ModelAndView("autoGatherApply").
        addObject("heading", "Auto Gathering")

      requestFilter.loadAttributesOntoRequest(request)
      val publisher = request.getAttribute("publisher").asInstanceOf[Website]
      mv.addObject("publisher", publisher)
      if (publisher != null) {
        val autotaggedResourceIds = request.getParameterValues("autotag")
        val resources = autotaggedResourceIds.flatMap(id => Await.result(mongoRepository.getResourceById(id), TenSeconds))
        val autotaggedNewsitems = resources.filter(resource => resource.`type` == "N").map { newsitem =>
          log.info("Applying publisher " + publisher.title + " to:" + newsitem.title)
          // TODO (newsitem.asInstanceOf[Newsitem]).setPublisher(publisher)
          contentUpdateService.update(newsitem)
          newsitem
        }

        mv.addObject("resources_to_tag", autotaggedNewsitems)
      }

      Await.result(withCommonLocal(mv), TenSeconds)
    }

    requiringAdminUser(apply)
  }

  private def getPossibleAutotagResources(publisher: Resource): Seq[Resource] = {
    val publishersHostname = urlParser.extractHostnameFrom(publisher.page)
    Await.result(mongoRepository.getNewsitemsMatchingHostname(publishersHostname), TenSeconds)
  }

  private def needsPublisher(resource: Newsitem, proposedPublisher: Website): Boolean = {
    if (resource.getPublisher == null) {
      true
    } else {
      if (resource.getPublisher != proposedPublisher) {
        true
      } else {
        false
      }
    }
  }

}
