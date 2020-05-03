package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{ContentRetrievalService, HandTaggingDAO}
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import nz.co.searchwellington.widgets.TagsWidgetFactory
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class NewsitemPageModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService,
                                                       taggingReturnsOfficerService: TaggingReturnsOfficerService,
                                                       tagWidgetFactory: TagsWidgetFactory,
                                                       tagVoteDAO: HandTaggingDAO,
                                                       mongoRepository: MongoRepository) extends ModelBuilder with ReasonableWaits {

  private val log = Logger.getLogger(classOf[NewsitemPageModelBuilder])

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/.*?/\\d\\d\\d\\d/[a-z]{3}/\\d\\d?/.*?$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: User): Future[Option[ModelAndView]] = {
    contentRetrievalService.getNewsPage(request.getPathInfo).map { frontendResource =>
      val mv = new ModelAndView
      mv.addObject("item", frontendResource)
      mv.addObject("heading", frontendResource.getName)
      if (frontendResource.getPlace != null) {
        mv.addObject("geocoded", List(frontendResource).asJava)
      }

      for {
        maybeResource <- mongoRepository.getResourceById(frontendResource.getId)
      } yield {
        maybeResource.map { resource => // TODO abit strange that we have to load this database object just to pass it as an argument to someone else
          mv.addObject("votes", taggingReturnsOfficerService.compileTaggingVotes(resource).asJava)
          mv.addObject("geotag_votes", taggingReturnsOfficerService.getGeotagVotesForResource(resource).asJava)
          mv.addObject("tag_select", tagWidgetFactory.createMultipleTagSelect(tagVoteDAO.getHandpickedTagsForThisResourceByUser(loggedInUser, resource)))
          mv
        }
      }
    }.getOrElse {
      Future.successful(None)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: User) {
    mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5, 1, loggedInUser = Option(loggedInUser)))
  }

  def getViewName(mv: ModelAndView): String = "newsitemPage"

}
