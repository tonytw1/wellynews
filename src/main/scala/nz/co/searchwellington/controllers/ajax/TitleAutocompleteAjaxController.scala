package nz.co.searchwellington.controllers.ajax

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.htmlparsing.TitleExtractor
import nz.co.searchwellington.http.WSHttpFetcher
import nz.co.searchwellington.repositories.elasticsearch.PublisherGuessingService
import org.apache.commons.lang3.StringEscapeUtils
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Controller
class TitleAutocompleteAjaxController @Autowired()(viewFactory: ViewFactory, loggedInUserFilter: LoggedInUserFilter,
                                                   httpFetcher: WSHttpFetcher, titleTrimmer: TitleTrimmer,
                                                   publisherGuessingService: PublisherGuessingService,
                                                   titleExtractor: TitleExtractor) extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[TitleAutocompleteAjaxController])

  @GetMapping(Array("/ajax/title-autofill"))
  def handleRequest(request: HttpServletRequest): ModelAndView = {
    val loggedInUser = loggedInUserFilter.getLoggedInUser

    /* Given the url of a new page fetch it and try to extract the HTML title.
    This can be used to prefill the title field with submitted a new resource.
    Restrict to admin users to prevent abuse.
    */
    val eventualTitle: Future[Option[String]] = Option(request.getParameter("url")).map { url =>
      val isAdmin = loggedInUser.exists(_.isAdmin)
      if (isAdmin) {

        try {
          val uri = java.net.URI.create(url)  // TODO use a Try instead

          httpFetcher.httpFetch(uri.toURL).map { r =>
            if (r.status == HttpServletResponse.SC_OK) {
              val maybePageTitle = titleExtractor.extractTitle(r.body)

              val maybeTrimmedTitle = maybePageTitle.map { pageTitle =>
                // HTML unescape
                val unescaped = StringEscapeUtils.unescapeHtml4(pageTitle)
                
                // Attempt to trim common seo publisher name suffix from title
                val maybePublisher = Option(request.getParameter("url")).flatMap { url =>
                  publisherGuessingService.guessPublisherBasedOnUrl(url, loggedInUser)
                }
                titleTrimmer.trimTitle(unescaped, maybePublisher)
              }

              Seq(maybeTrimmedTitle, maybePageTitle).flatten.headOption

            } else {
              None
            }
          }

        } catch {
          case _: IllegalArgumentException =>
            log.warn("Likely invalid url; ignoring: " + url)
            Future.successful(None)
          case _: Throwable =>
            Future.successful(None)
        }

      } else {
        Future.successful(None)
      }
    }.getOrElse(Future.successful(None))

    val title = Await.result(eventualTitle, TenSeconds).getOrElse("")
    log.info("Returning title: " + title)
    new ModelAndView(viewFactory.getJsonView).addObject("data", title)
  }

}
