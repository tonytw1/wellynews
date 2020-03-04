package nz.co.searchwellington.controllers.admin

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.CommonModelObjectsService
import nz.co.searchwellington.filters.AdminRequestFilter
import nz.co.searchwellington.model.{Newsitem, Resource, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.{ContentRetrievalService, HibernateResourceDAO}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlParser
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller class PublisherAutoGatherController @Autowired()(requestFilter: AdminRequestFilter, mongoRepository: MongoRepository, resourceDAO: HibernateResourceDAO,
                                                             contentUpdateService: ContentUpdateService,
                                                             urlParser: UrlParser,
                                                             val contentRetrievalService: ContentRetrievalService) extends ReasonableWaits with CommonModelObjectsService {

  private val log = Logger.getLogger(classOf[PublisherAutoGatherController])

  @RequestMapping(Array("/admin/gather/prompt")) def prompt(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
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

  @RequestMapping(value = Array("/admin/gather/apply"), method = Array(RequestMethod.POST)) def apply(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
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

  private def getPossibleAutotagResources(publisher: Resource): Seq[Resource] = {
    publisher.page.map { p =>
      val publishersUrlStem = urlParser.extractHostnameFrom(p)
      resourceDAO.getNewsitemsMatchingStem(publishersUrlStem)
    }.getOrElse(Seq.empty)
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
