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

import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
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

            val frontendGathered = Await.result(Future.sequence {
              gathered.map { resource =>
                frontendResourceMapper.createFrontendResourceFrom(resource, Some(loggedInUser))
              }
            }, TenSeconds)
            mv.addObject("gathered", frontendGathered.asJava)

          case _ =>
            null
        }
      }, TenSeconds).orNull
    }

    requiringAdminUser(prompt)
  }

  @PostMapping(Array("/admin/gather/{id}"))
  def apply(@PathVariable id: String, request: HttpServletRequest): ModelAndView = {
    def apply(loggedInUser: User): ModelAndView = {
      Await.result({
        mongoRepository.getResourceById(id).map { maybeResource =>
          maybeResource.map {
            case publisher: Website =>
              val autotaggedResourceIds = request.getParameterValues("autotag") // TODO parameter name
              val resources = autotaggedResourceIds.flatMap(id => Await.result(mongoRepository.getResourceById(id), TenSeconds))
              resources.foreach { resource =>
                resource match {
                  case published: PublishedResource =>
                    log.info("Applying publisher " + publisher.title + " to:" + publisher.title)
                    published.setPublisher(publisher)
                    Await.result(contentUpdateService.update(published), TenSeconds)
                  case _ =>
                }
              }
              new ModelAndView("autoGatherApply")
            case _ =>
              null
          }.orNull
        }
      }, TenSeconds)
    }

    requiringAdminUser(apply)
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
