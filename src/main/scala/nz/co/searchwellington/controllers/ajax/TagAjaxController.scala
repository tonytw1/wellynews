package nz.co.searchwellington.controllers.ajax

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory

import scala.concurrent.Future

@Controller class TagAjaxController @Autowired()(val loggedInUserFilter: LoggedInUserFilter,
                                                 val viewFactory: ViewFactory,
                                                 contentRetrievalService: ContentRetrievalService) extends BaseAjaxController with ReasonableWaits {

  private val log = Logger.getLogger(classOf[TagAjaxController])

  @RequestMapping(Array("/ajax/tags"))
  override def handleRequest(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    super.handleRequest(request, response)
  }

  override protected def getSuggestions(q: String, loggedInUser: Option[User]): Future[Seq[String]] = {
    log.debug("Looking up possible tags starting with: " + q)
    contentRetrievalService.getTagNamesStartingWith(q, loggedInUser)
  }

}
