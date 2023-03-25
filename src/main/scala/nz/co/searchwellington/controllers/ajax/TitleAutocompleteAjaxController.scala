package nz.co.searchwellington.controllers.ajax

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletResponse
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.htmlparsing.TitleExtractor
import nz.co.searchwellington.http.WSHttpFetcher
import nz.co.searchwellington.repositories.elasticsearch.PublisherGuessingService
import org.apache.commons.logging.LogFactory
import org.apache.commons.text.StringEscapeUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{GetMapping, RequestParam}
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory

import java.net.URL
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.util.Try

@Controller
class TitleAutocompleteAjaxController @Autowired()(viewFactory: ViewFactory, loggedInUserFilter: LoggedInUserFilter,
                                                   httpFetcher: WSHttpFetcher, titleTrimmer: TitleTrimmer,
                                                   publisherGuessingService: PublisherGuessingService,
                                                   titleExtractor: TitleExtractor) extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[TitleAutocompleteAjaxController])

  @GetMapping(Array("/ajax/title-autofill"))
  def handleRequest(@RequestParam url: String): ModelAndView = {
    implicit val currentSpan: Span = Span.current()
    val loggedInUser = loggedInUserFilter.getLoggedInUser

    val parsedUrl = Option(url).flatMap { urlString =>
      Try {
        new URL(urlString)
      }.toOption
    }

    /* Given the url of a new page fetch it and try to extract the HTML title.
        This can be used to prefill the title field with submitted a new resource.
        If we can determine the name of the publisher try to strip that from the title as well
        Restrict to admin users to prevent abuse.
        */
    val eventualTitle = parsedUrl.map { url =>
      if (loggedInUser.exists(_.isAdmin)) {
        val eventualMaybePageTitle = httpFetcher.httpFetch(url).map { r =>
          if (r.status == HttpServletResponse.SC_OK) {
            titleExtractor.extractTitle(r.body).map(StringEscapeUtils.unescapeHtml4)
          } else {
            None
          }
        }
        val eventualMaybePublisherName = publisherGuessingService.guessPublisherBasedOnUrl(url.toExternalForm, loggedInUser).map(_.map(_.title))

        for {
          maybePageTitle <- eventualMaybePageTitle
          maybePublisherName <- eventualMaybePublisherName

        } yield {
          maybePageTitle.map { pageTitle =>
            maybePublisherName.map { publisherName =>
              // Attempt to trim common seo publisher name suffix from title
              titleTrimmer.trimTitleSuffix(pageTitle, publisherName)
            }.getOrElse {
              pageTitle
            }
          }
        }

      } else {
        Future.successful(None)
      }
    }.getOrElse {
      Future.successful(None)
    }

    val title = Await.result(eventualTitle, TenSeconds).getOrElse("")
    log.info(s"Returning title '$title' for page")
    new ModelAndView(viewFactory.getJsonView).addObject("data", title)
  }

}
