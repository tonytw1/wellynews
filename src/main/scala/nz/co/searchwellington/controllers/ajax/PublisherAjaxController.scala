package nz.co.searchwellington.controllers.ajax

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
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
import scala.concurrent.ExecutionContext.Implicits.global

@Controller class PublisherAjaxController @Autowired()(val loggedInUserFilter: LoggedInUserFilter,
                                                       val viewFactory: ViewFactory,
                                                       contentRetrievalService: ContentRetrievalService)
  extends BaseAjaxController {

  private val log = Logger.getLogger(classOf[PublisherAjaxController])

  @RequestMapping(Array("/ajax/publishers"))
  override def handleRequest(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    super.handleRequest(request, response)
  }

  override protected def getSuggestions(q: String, loggedInUser: Option[User]): Future[Seq[String]] = {
    log.info("Looking up possible publishers starting with: " + q)
    contentRetrievalService.getPublisherNamesByStartingLetters(q, loggedInUser).map { publishers =>
      publishers.map(_.getTitle).filter(_.trim.nonEmpty)
    }
  }

}

