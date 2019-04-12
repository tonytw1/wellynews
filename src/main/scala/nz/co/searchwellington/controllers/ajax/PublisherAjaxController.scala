package nz.co.searchwellington.controllers.ajax

import java.io.IOException
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import nz.co.searchwellington.repositories.ContentRetrievalService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory

@Controller class PublisherAjaxController  @Autowired() (val viewFactory: ViewFactory, contentRetrievalService: ContentRetrievalService)   extends BaseAjaxController {

  private val log = Logger.getLogger(classOf[PublisherAjaxController])

  @RequestMapping(Array("/ajax/publishers"))
  override def handleRequest(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = super.handleRequest(request, response)

  override protected def getSuggestions(q: String): Seq[String] = {
    log.debug("Looking up possible publishers starting with: " + q)
    contentRetrievalService.getPublisherNamesByStartingLetters(q)
  }

}

