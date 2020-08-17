package nz.co.searchwellington.controllers.ajax

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.repositories.elasticsearch.PublisherGuessingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory

@Controller
class PublisherGuessController  @Autowired()(publisherGuessingService: PublisherGuessingService, viewFactory: ViewFactory, loggedInUserFilter: LoggedInUserFilter) {

  @RequestMapping(Array("/ajax/publisher-guess"))
  def handleRequest(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val maybePublisher = Option(request.getParameter("url")).flatMap { url =>
      publisherGuessingService.guessPublisherBasedOnUrl(url, loggedInUserFilter.getLoggedInUser)
    }

    val guesses = Seq(maybePublisher).flatten.map(_.title.getOrElse("")).filter(_.nonEmpty)

    import scala.collection.JavaConverters._
    new ModelAndView(viewFactory.getJsonView).addObject("data", guesses.asJava)
  }

}
