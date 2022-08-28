package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.taggingvotes.HandTagging
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap

import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Component class NewsitemPageModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                       taggingReturnsOfficerService: TaggingReturnsOfficerService,
                                                       mongoRepository: MongoRepository,
                                                       frontendResourceMapper: FrontendResourceMapper) extends ModelBuilder with ReasonableWaits {

  private val pattern = Pattern.compile("^/newsitem/(.*?)$")

  def isValid(request: HttpServletRequest): Boolean = {
    pattern.matcher(RequestPath.getPathFrom(request)).matches()
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[ModelMap]] = {
    val matcher = pattern.matcher(RequestPath.getPathFrom(request))
    if (matcher.matches()) {
      val id = matcher.group(1)
      mongoRepository.getResourceById(id).flatMap { maybeResource =>
        maybeResource.map { resource =>
          val eventualFrontendResource = frontendResourceMapper.createFrontendResourceFrom(resource, loggedInUser)
          val eventualGeotagVotes = taggingReturnsOfficerService.getGeotagVotesForResource(resource)
          val eventualTaggingVotes = taggingReturnsOfficerService.getTaggingsVotesForResource(resource)
          for {
            frontendResource <- eventualFrontendResource
            geotagVotes <- eventualGeotagVotes
            taggingVotes <- eventualTaggingVotes

          } yield {
            val mv = new ModelMap().
              addAttribute("item", frontendResource).
              addAttribute("heading", resource.title)

            val handTaggingVotes = taggingVotes.filter { vote =>
              vote match {
                case h: HandTagging => true
                case _ => false
              }
            }

            val otherTaggingVotes = taggingVotes.filterNot(handTaggingVotes.contains(_))

            mv.addAttribute("hand_tagging_votes", handTaggingVotes.asJava)
            mv.addAttribute("other_tagging_votes", otherTaggingVotes.asJava)
            mv.addAttribute("geotag_votes", geotagVotes.asJava)

            frontendResource.geocode.foreach { _ =>
              mv.addAttribute("geocoded", List(frontendResource).asJava)
            }

            Some(mv)
          }

        }.getOrElse {
          Future.successful(None)
        }
      }

    } else {
      Future.successful(None)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[ModelMap] = {
    latestNewsitems(loggedInUser)
  }

  def getViewName(mv: ModelMap, loggedInUser: Option[User]): String = "newsitemPage"

}
