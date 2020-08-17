package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Resource, User, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlParser
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{Await, Future}

@Component class PublisherGuessingService @Autowired()(contentRetrievalService: ContentRetrievalService, var urlParser: UrlParser) extends ReasonableWaits {
  /*
    Given the url of a new newsitem try to guess the publisher so that we can autocomplete the submission form.

    Most newsitems have the same hostname as their publisher and most publishers have a unique hostname.
    Given a newsitem then the publisher is likely to be one of the websites with the same hostname.
   */

  private val log = Logger.getLogger(classOf[PublisherGuessingService])

  def guessPublisherBasedOnUrl(url: String, loggedInUser: Option[User]): Option[Website] = {

    def guessPossiblePublishersForUrl(url: String): Future[Seq[Resource]] = {
      Option(urlParser.extractHostnameFrom(url)).map { hostname =>
        contentRetrievalService.getWebsitesByHostname(hostname, loggedInUser)
      }.getOrElse{
        Future.successful(Seq.empty)
      }
    }

    val possiblePublishers = Await.result(guessPossiblePublishersForUrl(url), TenSeconds)

    (if (possiblePublishers.size == 1) {
      possiblePublishers.headOption

    } else if (possiblePublishers.size > 1) {

      val filtered = possiblePublishers.filter { p =>
        val pageUrl = p.page
        url.startsWith(pageUrl)
      }

      filtered.headOption

    } else {
      None

    }).map { p =>
      p.asInstanceOf[Website]
    }
  }

}
