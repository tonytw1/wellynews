package nz.co.searchwellington.controllers.admin

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.{CommonModelObjectsService, LoggedInUserFilter, RequiringLoggedInUser}
import nz.co.searchwellington.filters.AdminRequestFilter
import nz.co.searchwellington.model._
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlParser
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{GetMapping, PostMapping}
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

  private val log = LogFactory.getLog(classOf[PublisherAutoGatherController])

  @GetMapping(Array("/admin/gather/prompt"))  // TODO incorrect path
  def prompt(request: HttpServletRequest): ModelAndView = {
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
      mv
    }

    requiringAdminUser(prompt)
  }

  @PostMapping(Array("/admin/gather/apply"))
  def apply(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    def apply(loggedInUser: User): ModelAndView = {
      val mv = new ModelAndView("autoGatherApply").
        addObject("heading", "Auto Gathering")

      requestFilter.loadAttributesOntoRequest(request)
      val publisher = request.getAttribute("publisher").asInstanceOf[Website]
      mv.addObject("publisher", publisher)
      if (publisher != null) {
        val autotaggedResourceIds = request.getParameterValues("autotag") // TODO parameter name
        val resources = autotaggedResourceIds.flatMap(id => Await.result(mongoRepository.getResourceById(id), TenSeconds))
        val autotaggedNewsitems = resources.filter(resource => resource.`type` == "N").map { newsitem =>
          log.info("Applying publisher " + publisher.title + " to:" + newsitem.title)
          newsitem match {
            case p: PublishedResource => p.setPublisher(publisher)
            case _ =>
          }
          contentUpdateService.update(newsitem)
          newsitem
        }

        mv.addObject("resources_to_tag", autotaggedNewsitems)
      }

      mv
    }

    requiringAdminUser(apply)
  }

  private def getPossibleAutotagResources(publisher: Resource): Seq[Resource] = {
    val publishersHostname = urlParser.extractHostnameFrom(publisher.page)
    Await.result(mongoRepository.getNewsitemsMatchingHostname(publishersHostname), TenSeconds)
  }

  private def needsPublisher(resource: Newsitem, proposedPublisher: Website): Boolean = {
    // Apply the new publisher if the resource currently has no publisher or a different publisher
    !resource.publisher.contains(proposedPublisher._id) // TODO inline this into the query
  }

}
