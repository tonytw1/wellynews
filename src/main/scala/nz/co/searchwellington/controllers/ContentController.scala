package nz.co.searchwellington.controllers

import java.io.IOException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import nz.co.searchwellington.annotations.Timed
import nz.co.searchwellington.controllers.models.ContentModelBuilderService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import com.sun.syndication.io.FeedException

@Controller class ContentController @Autowired()(contentModelBuilder: ContentModelBuilder, urlStack: UrlStack) {

  private val log = Logger.getLogger(classOf[ContentController])

  @RequestMapping(value = Array("/", "/*", "/search", "/archive/*/*", "/*/comment", "/*/geotagged", "/feed/*", "/feeds/inbox", "/tags", "/tags/json", "/*/json", "/*/rss", "/*/*/*/*/*"))
  @Timed(timingNotes = "")
  @throws[IllegalArgumentException]
  @throws[FeedException]
  @throws[IOException]
  def normal(request: Nothing, response: Nothing): Nothing = {
    val mvo = contentModelBuilder.populateContentModel(request)
    mvo.fold {
      ContentController.log.warn("Model was null; returning 404")
      response.setStatus(HttpServletResponse.SC_NOT_FOUND)
      null

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