package nz.co.searchwellington.controllers.admin

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
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
    implicit val currentSpan: Span = Span.current()

    def prompt(loggedInUser: User): ModelAndView = {
      val eventualModelAndView = mongoRepository.getResourceById(id).flatMap { maybeResource =>
        maybeResource.map {
          case publisher: Website =>
            for {
              frontendPublisher <- frontendResourceMapper.createFrontendResourceFrom(publisher)
              gathered <- getPossibleGatheredResources(publisher, loggedInUser)
              frontendGathered <- Future.sequence {
                gathered.map { resource =>
                  frontendResourceMapper.createFrontendResourceFrom(resource, Some(loggedInUser))
                }
              }
            } yield {
              new ModelAndView("gatherPrompt")
                .addObject("heading", "Auto Gathering")
                .addObject("publisher", frontendPublisher)
                .addObject("gathered", frontendGathered.asJava)
            }
          case _ =>
            Future.successful(null)
        }.getOrElse {
          Future.successful(null)
        }
      }
      Await.result(eventualModelAndView, TenSeconds)
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
              resources.foreach {
                case published: PublishedResource =>
                  log.info("Applying publisher " + publisher.title + " to:" + publisher.title)
                  published.setPublisher(publisher)
                  Await.result(contentUpdateService.update(published), TenSeconds)
                case _ =>
              }
              new ModelAndView("gatherApply")
            case _ =>
              null
          }.orNull
        }
      }, TenSeconds)
    }

    requiringAdminUser(apply)
  }

  private def getPossibleGatheredResources(publisher: Website, loggedInUser: User)(implicit currentSpan: Span): Future[Seq[Resource]] = {
    urlParser.extractHostnameFrom(publisher.page).map { publishersHostname =>
      contentRetrievalService.getPublishedResourcesMatchingHostname(publisher,
        publishersHostname, Some(loggedInUser)).map { gathered =>
        log.info("Found  " + gathered.size + " newsitems to gather for publisher: " + publisher.title)
        gathered
      }
    }.getOrElse{
      Future.successful(Seq.empty)
    }
  }

}
