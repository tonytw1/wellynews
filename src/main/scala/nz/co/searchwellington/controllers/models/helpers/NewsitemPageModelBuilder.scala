package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.taggingvotes.{HandTagging, TaggingVote}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap
import org.springframework.web.servlet.ModelAndView

import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

@Component class NewsitemPageModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                       taggingReturnsOfficerService: TaggingReturnsOfficerService,
                                                       mongoRepository: MongoRepository,
                                                       frontendResourceMapper: FrontendResourceMapper) extends ModelBuilder with ReasonableWaits {

  private val pattern = Pattern.compile("^/newsitem/(.*?)$")

  def isValid(request: HttpServletRequest): Boolean = {
    pattern.matcher(RequestPath.getPathFrom(request)).matches()
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {
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
            val mv = new ModelAndView
            mv.addObject("item", frontendResource)
            mv.addObject("heading", resource.title.orNull)

            val handTagging = taggingVotes.flatMap { vote =>
              vote match {
                case h: HandTagging => Some(h)
                case _ => None
              }
            }

            mv.addObject("hand_taggings", handTagging.asJava)
            mv.addObject("geotag_votes", geotagVotes.asJava)
            mv.addObject("tagging_votes", taggingVotes.asJava)

            if (frontendResource.getPlace != null) {
              mv.addObject("geocoded", List(frontendResource).asJava)
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

  def populateExtraModelContent(request: HttpServletRequest, loggedInUser: Option[User]): Future[ModelMap] = {
    latestNewsitems(loggedInUser)
  }

  def getViewName(mv: ModelAndView, loggedInUser: Option[User]): String = "newsitemPage"

}
