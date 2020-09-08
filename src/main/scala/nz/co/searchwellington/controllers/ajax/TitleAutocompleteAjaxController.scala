package nz.co.searchwellington.controllers.ajax

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.htmlparsing.SnapshotBodyExtractor
import nz.co.searchwellington.http.WSHttpFetcher
import nz.co.searchwellington.repositories.elasticsearch.PublisherGuessingService
import org.apache.log4j.Logger
import org.htmlparser.Parser
import org.htmlparser.filters.TagNameFilter
import org.htmlparser.util.ParserException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

@Controller
class TitleAutocompleteAjaxController @Autowired()(viewFactory: ViewFactory, loggedInUserFilter: LoggedInUserFilter,
                                                   httpFetcher: WSHttpFetcher, titleTrimmer: TitleTrimmer,
                                                   publisherGuessingService: PublisherGuessingService) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[SnapshotBodyExtractor])

  @RequestMapping(Array("/ajax/title-autofill"))
  def handleRequest(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    /* Given the url of a new page fetch it and try to extract the HTML title.
    This can be used to prefill the title field with submitted a new resource.
    Restrict to admin users to prevent abuse.
    */

    val eventualTitle: Future[Option[String]] = Option(request.getParameter("url")).map { url =>
      val isAdmin = loggedInUserFilter.getLoggedInUser.exists(_.isAdmin)
      if (isAdmin) {
        val uri = java.net.URI.create(url) // TODO exception check

        httpFetcher.httpFetch(uri.toString).map { r =>
          if (r.status == HttpServletResponse.SC_OK) {
            val maybePageTitle = extractTitle(r.body)

            val maybeTrimmedTitle = maybePageTitle.map { pageTitle =>
              // Attempt to trim common seo publisher name suffix from title
              val maybePublisher = Option(request.getParameter("url")).flatMap { url =>
                publisherGuessingService.guessPublisherBasedOnUrl(url, loggedInUserFilter.getLoggedInUser)
              }
              titleTrimmer.trimTitle(pageTitle, maybePublisher);
            }

            Seq(maybeTrimmedTitle, maybePageTitle).flatten.headOption

          } else {
            None
          }
        }
      } else {
        Future.successful(None)
      }
    }.getOrElse(Future.successful(None))

    val title = Await.result(eventualTitle, TenSeconds).getOrElse("")
    log.info("Returning title: " + title)
    new ModelAndView(viewFactory.getJsonView).addObject("data", title)
  }

  def extractTitle(htmlPage: String): Option[String] = {
    log.info("Extracting title")
    try {
      val parser = new Parser
      parser.setInputHTML(htmlPage)

      val titleTagFilter = new TagNameFilter("TITLE")
      val list = parser.extractAllNodesThatMatch(titleTagFilter)
      log.info("Found matching nodes: " + list.size())
      if (list.size > 0) {
        val title = list.elementAt(0)
        log.info("Found title: " + title)
        Some(title.toPlainTextString)
      } else {
        log.info("No title found")
        None
      }

    } catch {
      case e: ParserException =>
        log.warn("Parser exception while extracting title", e)
        None
      case e: Exception =>
        log.error("Exception while extracting title", e)
        None
    }
  }

}
