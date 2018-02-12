package nz.co.searchwellington.controllers

import java.io.IOException
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.sun.syndication.io.FeedException
import nz.co.searchwellington.annotations.Timed
import nz.co.searchwellington.controllers.models.ContentModelBuilderService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller class ContentController @Autowired() (contentModelBuilderService: ContentModelBuilderService, urlStack: UrlStack) {

  private val log = Logger.getLogger(classOf[ContentController])

  @RequestMapping(value = Array("/", "/*", "/search", "/archive/*/*", "/*/comment", "/*/geotagged", "/feed/*", "/feeds/inbox", "/tags", "/tags/json", "/*/json", "/*/rss", "/*/*/*/*/*"))
  @Timed(timingNotes = "")
  @throws[IllegalArgumentException]
  @throws[FeedException]
  @throws[IOException]
  def normal(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mvo = contentModelBuilderService.populateContentModel(request)
    mvo.fold {
      log.warn("Model was null; returning 404")
      response.setStatus(HttpServletResponse.SC_NOT_FOUND)
      val a: ModelAndView = null
      a

    } { mv =>
      if (isHtmlView(mv)) {
        urlStack.setUrlStack(request)
      }
      mv
    }
  }

  private def isHtmlView(mv: ModelAndView): Boolean = {
    return mv.getViewName != null
  }

}
