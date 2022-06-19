package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{User, Website}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class PublisherGuessingService @Autowired()(contentRetrievalService: ContentRetrievalService, var urlParser: UrlParser) extends ReasonableWaits {

  /*
    Given the url of a new published resource, try to guess the publisher so that we can autocomplete the submission form.
    Most newsitems have the same hostname as their publisher and publishers generally have a unique hostname.
    Given a newsitem then the publisher is likely to be one of the websites with the same hostname.
   */
  def guessPublisherBasedOnUrl(url: String, loggedInUser: Option[User])(implicit ec: ExecutionContext): Future[Option[Website]] = {

    def guessPossiblePublishersForUrl(url: String): Future[Seq[Website]] = {
      val eventualMatchingResources = urlParser.extractHostnameFrom(url).map { hostname =>
        contentRetrievalService.getWebsitesByHostname(hostname, loggedInUser)
      }.getOrElse {
        Future.successful(Seq.empty)
      }
      eventualMatchingResources.map { resources =>
        resources.flatMap {
          case w: Website => Some(w)
          case _ => None
        }
      }
    }

    guessPossiblePublishersForUrl(url).map { possiblePublishers: Seq[Website] =>
      if (possiblePublishers.size == 1) {
        possiblePublishers.headOption

      } else if (possiblePublishers.size > 1) {
        // If more than one publisher has this hostname tie break by
        // TODO incomplete implementation
        val filtered = possiblePublishers.filter { p =>
          val pageUrl = p.page
          url.startsWith(pageUrl)
        }
        filtered.headOption

      } else {
        None
      }
    }
  }

}
