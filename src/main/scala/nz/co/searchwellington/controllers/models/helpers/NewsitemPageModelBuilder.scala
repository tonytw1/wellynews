package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{ContentRetrievalService, HandTaggingDAO}
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

@Component class NewsitemPageModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                       taggingReturnsOfficerService: TaggingReturnsOfficerService,
                                                       handTaggingDAO: HandTaggingDAO,
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
          val eventualHandTaggings = handTaggingDAO.getHandTaggingsForResource(resource)  // TODO should be able to filter this from the tagging votes
          val eventualGeotagVotes = taggingReturnsOfficerService.getGeotagVotesForResource(resource)
          val eventualTaggingVotes = taggingReturnsOfficerService.getTaggingsVotesForResource(resource)
          for {
            frontendResource <- eventualFrontendResource
            handTaggings <- eventualHandTaggings
            geotagVotes <- eventualGeotagVotes
            taggingVotes <- eventualTaggingVotes

          } yield {
            val mv = new ModelAndView
            mv.addObject("item", frontendResource)
            mv.addObject("heading", resource.title.orNull)

            mv.addObject("hand_taggings", handTaggings.asJava)
            mv.addObject("geotag_votes", geotagVotes.asJava)
            mv.addObject("tagging_votes", taggingVotes.asJava)

            if (frontendResource.getPlace != null) {
              mv.addObject("geocoded", List(frontendResource).asJava)
            }

            //for {
            // maybeResource <- mongoRepository.getResourceById(resource.getId)
            //} yield {
            // maybeResource.map { resource => // TODO abit strange that we have to load this database object just to pass it as an argument to someone else
            //mv.addObject("votes", taggingReturnsOfficerService.compileTaggingVotes(resource).asJava)
            //mv.addObject("geotag_votes", taggingReturnsOfficerService.getGeotagVotesForResource(resource).asJava)
            //mv.addObject("tag_select", tagWidgetFactory.createMultipleTagSelect(tagVoteDAO.getHandpickedTagsForThisResourceByUser(loggedInUser, resource)))
            //   mv
            // }
            // }
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

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = withLatestNewsitems(mv, loggedInUser)

  def getViewName(mv: ModelAndView, loggedInUser: Option[User]): String = "newsitemPage"

}
