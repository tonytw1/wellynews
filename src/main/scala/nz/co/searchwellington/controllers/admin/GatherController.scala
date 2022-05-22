package nz.co.searchwellington.controllers.admin

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.{CommonModelObjectsService, LoggedInUserFilter, RequiringLoggedInUser}
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlParser
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{GetMapping, PathVariable, PostMapping}
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters._

@Controller class GatherController @Autowired()(mongoRepository: MongoRepository,
                                                contentUpdateService: ContentUpdateService,
                                                urlParser: UrlParser,
                                                val contentRetrievalService: ContentRetrievalService,
                                                val frontendResourceMapper: FrontendResourceMapper,
                                                val loggedInUserFilter: LoggedInUserFilter) extends
  ReasonableWaits with CommonModelObjectsService with RequiringLoggedInUser {

  private val log = LogFactory.getLog(classOf[GatherController])

  @GetMapping(Array("/admin/gather/{id}"))
  def prompt(@PathVariable id: String): ModelAndView = {
    def prompt(loggedInUser: User): ModelAndView = {
      Await.result(mongoRepository.getResourceById(id).map { maybeResource =>
        maybeResource.map {
          case publisher: Website =>
            val frontendPublisher = Await.result(frontendResourceMapper.createFrontendResourceFrom(publisher), TenSeconds)
            val mv = new ModelAndView("gatherPrompt").
              addObject("heading", "Auto Gathering")
              .addObject("publisher", frontendPublisher)

            val gathered = getPossibleGatheredResources(publisher, loggedInUser).filter { resource =>
              needsPublisher(resource, publisher)
            }
            mv.addObject("gathered", gathered.asJava)
          case _ =>
            null
        }
      }, TenSeconds).orNull
    }

    requiringAdminUser(prompt)
  }

  @PostMapping(Array("/admin/gather/apply"))
  def apply(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    /*
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
    */
    ???
  }

  private def getPossibleGatheredResources(publisher: Resource, loggedInUser: User): Seq[Resource] = {
    val publishersHostname = urlParser.extractHostnameFrom(publisher.page)
    val newsitemsByHostname = Await.result(contentRetrievalService.getPublishedResourcesMatchingHostname(publishersHostname, Some(loggedInUser)), TenSeconds)
    log.info("Gathered " + newsitemsByHostname.size + " newsitems by hostname: " + publishersHostname)
    newsitemsByHostname
  }

  private def needsPublisher(resource: Resource, proposedPublisher: Website): Boolean = { // TODO inline this filter into the query
    // Apply the new publisher if the resource currently has no publisher or a different publisher
    resource match {
      case published: PublishedResource =>
        !published.publisher.contains(proposedPublisher._id)
      case _ =>
        false
    }
  }

}
