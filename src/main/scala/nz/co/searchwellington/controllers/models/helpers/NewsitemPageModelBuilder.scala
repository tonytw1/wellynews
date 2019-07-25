package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{ContentRetrievalService, HandTaggingDAO}
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import nz.co.searchwellington.widgets.TagsWidgetFactory
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Component class NewsitemPageModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService,
                                                       taggingReturnsOfficerService: TaggingReturnsOfficerService,
                                                       tagWidgetFactory: TagsWidgetFactory,
                                                       tagVoteDAO: HandTaggingDAO,
                                                       loggedInUserFilter: LoggedInUserFilter,
                                                       mongoRepository: MongoRepository) extends ModelBuilder with ReasonableWaits {

  private val log = Logger.getLogger(classOf[NewsitemPageModelBuilder])

  def getViewName(mv: ModelAndView): String = {
    "newsitemPage"
  }

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/.*?/\\d\\d\\d\\d/[a-z]{3}/\\d\\d?/.*?$")
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {
    contentRetrievalService.getNewsPage(request.getPathInfo).map { frontendResource =>
      val mv = new ModelAndView
      mv.addObject("item", frontendResource)
      mv.addObject("heading", frontendResource.getName)
      if (frontendResource.getPlace != null) {
        mv.addObject("geocoded", List(frontendResource).asJava)
      }

      Await.result(mongoRepository.getResourceById(frontendResource.getId), TenSeconds).map { resource => // TODO abit strange that we have to load this database object just to pass it as an argument to someone else
        mv.addObject("votes", taggingReturnsOfficerService.compileTaggingVotes(resource).asJava)
        mv.addObject("geotag_votes", taggingReturnsOfficerService.getGeotagVotesForResource(resource).asJava)
        mv.addObject("tag_select", tagWidgetFactory.createMultipleTagSelect(tagVoteDAO.getHandpickedTagsForThisResourceByUser(loggedInUserFilter.getLoggedInUser, resource)))
      }
      mv
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5, 1, loggedInUser = Option(loggedInUserFilter.getLoggedInUser)))
  }

}
