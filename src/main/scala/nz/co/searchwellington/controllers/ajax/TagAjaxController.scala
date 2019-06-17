package nz.co.searchwellington.controllers.ajax

import java.io.IOException

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory

import scala.concurrent.Await

@Controller class TagAjaxController @Autowired()(val viewFactory: ViewFactory, var contentRetrievalService: ContentRetrievalService) extends BaseAjaxController with ReasonableWaits {

  private val log = Logger.getLogger(classOf[TagAjaxController])

  @RequestMapping(Array("/ajax/tags"))
  @throws[IOException]
  override def handleRequest(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    super.handleRequest(request, response)
  }

  override protected def getSuggestions(q: String): Seq[String] = {
    log.debug("Looking up possible tags starting with: " + q)
    Await.result(contentRetrievalService.getTagNamesStartingWith(q), TenSeconds)
  }

}
