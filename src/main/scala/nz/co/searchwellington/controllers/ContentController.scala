package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.ContentModelBuilderServiceFactory
import nz.co.searchwellington.filters.RequestPath
import org.apache.commons.logging.LogFactory
import org.joda.time.{DateTime, Duration}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.concurrent.Await

@Order(5)
@Controller
class ContentController @Autowired()(contentModelBuilderServiceFactory: ContentModelBuilderServiceFactory, urlStack: UrlStack, loggedInUserFilter: LoggedInUserFilter) extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[ContentController])

  private val contentModelBuilderService = contentModelBuilderServiceFactory.makeContentModelBuilderService()

  // Ant-style path patterns
  @GetMapping(path = Array("/", "/*", "/search", "/archive/*", "/profiles/**", "/*/geotagged", "/feed/*",
    "/feed/*/json", "/feeds/inbox", "/profiles", "/publishers", "/publishers/json", "/tags", "/tags/json", "/*/json", "/*/rss",
    "/*/geotagged/rss", "/*/geotagged/json",
    "/newsitem/*",
    "/{\\w+}/{year:\\d+}-{month:\\w+}"
  ))
  def normal(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val start = new DateTime()
    val eventualMaybeView = contentModelBuilderService.populateContentModel(request, loggedInUserFilter.getLoggedInUser)
    try {
      Await.result(eventualMaybeView, TenSeconds).fold {
        log.warn("Model was null; returning 404")
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found")

      } { mv =>
        if (isHtmlView(mv)) {
          urlStack.setUrlStack(request)
        }
        mv
      }
    } catch {
      case r: ResponseStatusException =>
        throw r
      case e: Exception =>
        log.error("Error building content", e)
        throw e
    }
  }

  private def isHtmlView(mv: ModelAndView): Boolean = mv.getViewName != null

}
