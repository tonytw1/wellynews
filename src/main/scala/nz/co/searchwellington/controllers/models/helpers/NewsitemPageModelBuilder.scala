package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest

import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.repositories.{ContentRetrievalService, HandTaggingDAO, HibernateResourceDAO}
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import nz.co.searchwellington.widgets.TagsWidgetFactory
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.collection.JavaConverters._

@Component class NewsitemPageModelBuilder @Autowired() (contentRetrievalService: ContentRetrievalService,
                                                       taggingReturnsOfficerService: TaggingReturnsOfficerService,
                                                       tagWidgetFactory: TagsWidgetFactory,
                                                       tagVoteDAO: HandTaggingDAO,
                                                       loggedInUserFilter: LoggedInUserFilter,
                                                       resourceDAO: HibernateResourceDAO) extends ModelBuilder {

  private val log = Logger.getLogger(classOf[NewsitemPageModelBuilder])

  def getViewName(mv: ModelAndView): String = {
    "newsitemPage"
  }

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/.*?/\\d\\d\\d\\d/[a-z]{3}/\\d\\d?/.*?$")
  }

  def populateContentModel(request: HttpServletRequest): Option[ModelAndView] = {
    val frontendResource = contentRetrievalService.getNewsPage(request.getPathInfo)
    if (frontendResource != null) {
      val mv = new ModelAndView
      mv.addObject("item", frontendResource)
      mv.addObject("heading", frontendResource.getName)
      if (frontendResource.getPlace != null) {
        mv.addObject("geocoded", List(frontendResource).asJava)
      }

      resourceDAO.loadResourceById(frontendResource.getId).map { resource => // TODO abit strange that we have to load this database object just to pass it as an argument to someone else
        mv.addObject("votes", taggingReturnsOfficerService.compileTaggingVotes(resource).asJava)
        mv.addObject("geotag_votes", taggingReturnsOfficerService.getGeotagVotesForResource(resource).asJava)
        mv.addObject("tag_select", tagWidgetFactory.createMultipleTagSelect(tagVoteDAO.getHandpickedTagsForThisResourceByUser(loggedInUserFilter.getLoggedInUser, resource)))
      }

      Some(mv)

    } else {
      None
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
    mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5, 1))
  }

}
