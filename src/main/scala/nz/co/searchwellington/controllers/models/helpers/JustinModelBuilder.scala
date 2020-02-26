package nz.co.searchwellington.controllers.models.helpers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.ModelBuilder
import nz.co.searchwellington.controllers.{LoggedInUserFilter, RssUrlBuilder}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class JustinModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService,
                                                 rssUrlBuilder: RssUrlBuilder, urlBuilder: UrlBuilder, commonAttributesModelBuilder: CommonAttributesModelBuilder,
                                                 loggedInUserFilter: LoggedInUserFilter)
  extends ModelBuilder with CommonSizes with ReasonableWaits {

  def isValid(request: HttpServletRequest): Boolean = {
    request.getPathInfo.matches("^/justin(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest): Future[Option[ModelAndView]] = {
    if (isValid(request)) {
      for {
        websites <- contentRetrievalService.getLatestWebsites(MAX_NEWSITEMS, loggedInUser = Option(loggedInUserFilter.getLoggedInUser))
      } yield {
        import scala.collection.JavaConverters._
        val mv = new ModelAndView().
          addObject("heading", "Latest additions").
          addObject("description", "The most recently submitted website listings.").
          addObject("link", urlBuilder.getJustinUrl).
          addObject(MAIN_CONTENT, websites.asJava)

        commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForJustin, rssUrlBuilder.getRssUrlForJustin)
        Some(mv)
      }

    } else {
      Future.successful(None)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView) {
  }

  def getViewName(mv: ModelAndView) = "justin"

}
