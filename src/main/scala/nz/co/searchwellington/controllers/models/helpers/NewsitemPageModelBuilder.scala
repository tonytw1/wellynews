package nz.co.searchwellington.controllers.models.helpers

import java.util.regex.Pattern

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{ContentRetrievalService, HandTaggingDAO}
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import nz.co.searchwellington.widgets.TagsWidgetFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class NewsitemPageModelBuilder @Autowired()(val contentRetrievalService: ContentRetrievalService,
                                                       taggingReturnsOfficerService: TaggingReturnsOfficerService,
                                                       tagWidgetFactory: TagsWidgetFactory,
                                                       tagVoteDAO: HandTaggingDAO,
                                                       mongoRepository: MongoRepository,
                                                       frontendResourceMapper: FrontendResourceMapper) extends ModelBuilder with ReasonableWaits {

  val pattern = Pattern.compile("^/newsitem/(.*?)$")

  def isValid(request: HttpServletRequest): Boolean = {
    pattern.matcher(RequestPath.getPathFrom(request)).matches()
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {
    val matcher = pattern.matcher(RequestPath.getPathFrom(request))
    if (matcher.matches()) {
      val id = matcher.group(1)
      mongoRepository.getResourceById(id).flatMap { maybeResouce =>
        maybeResouce.map { resource =>
          val eventualFrontendResource = frontendResourceMapper.createFrontendResourceFrom(resource)
          val eventualHandTaggings = taggingReturnsOfficerService.getHandTaggingsForResource(resource)
          val eventualGeotagVotes = taggingReturnsOfficerService.getGeotagVotesForResource(resource)
          val eventualIndexTaggings = taggingReturnsOfficerService.getIndexTaggingsForResource(resource)
          for {
            frontendResource <- eventualFrontendResource
            handTaggings <- eventualHandTaggings
            geotagVotes <- eventualGeotagVotes
            indexTaggings <- eventualIndexTaggings

          } yield {
            val mv = new ModelAndView
            mv.addObject("item", frontendResource)
            mv.addObject("heading", resource.title.orNull)

            import scala.collection.JavaConverters._
            mv.addObject("hand_taggings", handTaggings.asJava)
            mv.addObject("geotag_votes", geotagVotes.asJava)
            mv.addObject("index_taggings", indexTaggings.asJava)

            //if (resource.getPlace != null) {
            // mv.addObject("geocoded", List(resource).asJava)
            //}

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

  def getViewName(mv: ModelAndView): String = "newsitemPage"

}
