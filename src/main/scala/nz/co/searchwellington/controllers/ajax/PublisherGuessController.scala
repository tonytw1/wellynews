package nz.co.searchwellington.controllers.ajax

import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.repositories.elasticsearch.PublisherGuessingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.jdk.CollectionConverters._
import scala.concurrent.ExecutionContext.Implicits.global

@Controller
class PublisherGuessController @Autowired()(publisherGuessingService: PublisherGuessingService, viewFactory: ViewFactory, loggedInUserFilter: LoggedInUserFilter) {

  @GetMapping(Array("/ajax/publisher-guess"))
  def handleRequest(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val maybePublisher = Option(request.getParameter("url")).flatMap { url =>
      publisherGuessingService.guessPublisherBasedOnUrl(url, loggedInUserFilter.getLoggedInUser)
    }

    val guesses = Seq(maybePublisher).flatten.map(_.title).filter(_.nonEmpty)

    new ModelAndView(viewFactory.getJsonView).addObject("data", guesses.asJava)
  }

}
