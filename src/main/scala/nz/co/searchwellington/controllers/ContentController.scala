package nz.co.searchwellington.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.annotations.Timed
import nz.co.searchwellington.controllers.models.ContentModelBuilderServiceFactory
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Order(3)
@Controller
class ContentController @Autowired()(contentModelBuilderServiceFactory: ContentModelBuilderServiceFactory, urlStack: UrlStack) {

  private val log = Logger.getLogger(classOf[ContentController])

  private val contentModelBuilderService = contentModelBuilderServiceFactory.makeContentModelBuilderService()

  @RequestMapping(value = Array("/", "/*", "/search", "/archive/*/*", "/*/comment", "/*/geotagged", "/feed/*", "/feeds/inbox", "/publishers", "/publishers/json", "/tags", "/tags/json", "/*/json", "/*/rss", "/*/*/*/*/*"))
  @Timed(timingNotes = "")
  def normal(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    contentModelBuilderService.populateContentModel(request).fold {
      log.warn("Model was null; returning 404")
      response.setStatus(HttpServletResponse.SC_NOT_FOUND)
      null: ModelAndView

    } { mv =>
      if (isHtmlView(mv)) {
        urlStack.setUrlStack(request)
      }
      mv
    }
  }

  private def isHtmlView(mv: ModelAndView): Boolean = mv.getViewName != null

}
