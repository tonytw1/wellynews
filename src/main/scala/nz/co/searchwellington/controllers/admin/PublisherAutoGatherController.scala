package nz.co.searchwellington.controllers.admin

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import nz.co.searchwellington.controllers.CommonModelObjectsService
import nz.co.searchwellington.model.{Newsitem, Resource, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.HibernateResourceDAO
import nz.co.searchwellington.urls.UrlParser
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView

@Controller class PublisherAutoGatherController @Autowired() (requestFilter: AdminRequestFilter, resourceDAO: HibernateResourceDAO, contentUpdateService: ContentUpdateService, commonModelObjectsService: CommonModelObjectsService, urlParser: UrlParser) {

  private val log = Logger.getLogger(classOf[PublisherAutoGatherController])

  @RequestMapping(Array("/admin/gather/prompt")) def prompt(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView
    mv.setViewName("autoGatherPrompt")
    mv.addObject("heading", "Auto Gathering")
    commonModelObjectsService.populateCommonLocal(mv)
    requestFilter.loadAttributesOntoRequest(request)
    val publisher: Website = request.getAttribute("publisher").asInstanceOf[Website]
    mv.addObject("publisher", publisher)
    if (publisher != null) {
      val resourcesToAutoTag = getPossibleAutotagResources(publisher).filter { resource =>
        needsPublisher(resource.asInstanceOf[Newsitem], publisher)
      }
      mv.addObject("resources_to_tag", resourcesToAutoTag)
    }
    mv
  }

  @RequestMapping(value = Array("/admin/gather/apply"), method = Array(RequestMethod.POST)) def apply(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView
    mv.setViewName("autoGatherApply")
    mv.addObject("heading", "Auto Gathering")
    commonModelObjectsService.populateCommonLocal(mv)
    requestFilter.loadAttributesOntoRequest(request)
    val publisher = request.getAttribute("publisher").asInstanceOf[Website]
    mv.addObject("publisher", publisher)
    if (publisher != null) {
      val autotaggedResourceIds = request.getParameterValues("autotag")
      val resources = autotaggedResourceIds.map { resourceIdString =>
        resourceDAO.loadResourceById(resourceIdString.toInt)
      }.flatten

      val autotaggedNewsitems = resources.filter(resource => resource.getType == "N").map { newsitem =>
        log.info("Applying publisher " + publisher.getName + " to:" + newsitem.getName)
        (newsitem.asInstanceOf[Newsitem]).setPublisher(publisher)
        contentUpdateService.update(newsitem)
        newsitem
      }

      mv.addObject("resources_to_tag", autotaggedNewsitems)
    }
    mv
  }

  private def getPossibleAutotagResources(publisher: Resource): Seq[Resource] = {
    val publishersUrlStem = urlParser.extractHostnameFrom(publisher.getUrl)
    resourceDAO.getNewsitemsMatchingStem(publishersUrlStem)
  }

  private def needsPublisher(resource: Newsitem, proposedPublisher: Website): Boolean = {
    if (resource.getPublisher == null) {
      true
    } else {
      if (resource.getPublisher ne proposedPublisher) {
        true
      } else {
        false
      }
    }
  }

}