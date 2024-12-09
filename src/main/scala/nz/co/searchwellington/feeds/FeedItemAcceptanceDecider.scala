package nz.co.searchwellington.feeds

import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.FeedAcceptancePolicy
import nz.co.searchwellington.repositories.SuppressionDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.{UrlCleaner, UrlFilters}
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.net.{MalformedURLException, URISyntaxException, URL}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Component class FeedItemAcceptanceDecider @Autowired()(mongoRepository: MongoRepository, suppressionDAO: SuppressionDAO) {

  private val log = LogFactory.getLog(classOf[FeedItemAcceptanceDecider])

  def getAcceptanceErrors(feedItem: FeedItem, acceptancePolicy: FeedAcceptancePolicy)(implicit ec: ExecutionContext): Future[Seq[String]] = {

    val cannotBeSuppressed = (feedItem: FeedItem, url: String) => {
      suppressionDAO.isSupressed(feedItem.url).map { isSuppressed =>
        log.info("Is feed item url '" + feedItem.url + "' suppressed: " + isSuppressed)
        if (isSuppressed) Some("This item is suppressed") else None
      }
    }

    val titleCannotBeBlank = (feedItem: FeedItem, url: String) => {
      Future.successful {
        if (feedItem.title.forall(_.trim.isEmpty)) {
          Some("Item has no title")
        } else {
          None
        }
      }
    }

    val cannotBeMoreThanOneWeekOld = (feedItem: FeedItem, url: String) => {
      Future.successful {
        if (acceptancePolicy == FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES) {
          None

        } else {
          if (feedItem.date.isEmpty) {
            Some("Item has no date and feed acceptance policy is not accept even without dates")

          } else {
            feedItem.date.flatMap { date =>
              val oneWeekAgo = DateTime.now.minusWeeks(1)
              val isMoreThanOneWeekOld = new DateTime(date).isBefore(oneWeekAgo)
              if (isMoreThanOneWeekOld) {
                Some("Item is more than one week old")
              } else {
                None
              }
            }
          }
        }
      }
    }

    val cannotBeMoreThanOneWeekInTheFuture = (feedItem: FeedItem, url: String) => {
      Future.successful {
        feedItem.date.flatMap { date =>
          val oneWeekFromNow = DateTime.now.plusWeeks(1)
          val isMoreThanOneWeekFromNow = new DateTime(date).isAfter(oneWeekFromNow)
          if (isMoreThanOneWeekFromNow) {
            Some("This item has a date more than one week in the future")
          } else {
            None
          }
        }
      }
    }

    val cannotAlreadyHaveThisFeedItem = (feedItem: FeedItem, url: String) => {
      val eventualAlreadyHaveThisFeedItem = mongoRepository.getResourceByUrl(url).map(_.nonEmpty)
      eventualAlreadyHaveThisFeedItem.map { alreadyHaveThisFeedItem =>
        log.info("Resource with url '" + url + "' already exists: " + alreadyHaveThisFeedItem)
        if (alreadyHaveThisFeedItem) {
          log.debug("A resource with url '" + url + "' already exists; not accepting.")
          Some("Item already exists")
        } else {
          None
        }
      }
    }

    val alreadyHaveAnItemWithTheSameHeadlineFromTheSamePublisherWithinTheLastMonth = (_: FeedItem, url: String) => {
      Future.successful(None)
    } // TODO implement me

    val reasonsToRejectFeedItems = Seq(
      cannotAlreadyHaveThisFeedItem,
      cannotBeSuppressed,
      titleCannotBeBlank,
      cannotBeMoreThanOneWeekOld,
      cannotBeMoreThanOneWeekInTheFuture,
      alreadyHaveAnItemWithTheSameHeadlineFromTheSamePublisherWithinTheLastMonth)


    def cleanUrl(urlString: String): Either[Throwable, URL] = {

      def cleanSubmittedItemUrl(url: URL): URL = try {  // TODO duplication
        // Resolve short urls
        var expanded = url

        // Strip obvious per request artifacts from the url to help with duplicate detection
        expanded = UrlFilters.stripUTMParams(expanded)
        expanded = UrlFilters.stripPhpSession(expanded)

        log.debug("Cleaned url is: " + expanded.toExternalForm)
        expanded

      } catch {
        case _: URISyntaxException | _: MalformedURLException =>
          log.warn("Invalid URL given; returning unaltered: " + url.toExternalForm)
          url
      }

      // Trim and add prefix is missing from user submitted input
      val cleanedString = UrlFilters.addHttpPrefixIfMissing(urlString.trim)
      Try {
        val url = new URL(cleanedString)
        cleanSubmittedItemUrl(url)
      }.toEither
    }

    cleanUrl(feedItem.url).toOption.map { url =>
      Future.sequence(reasonsToRejectFeedItems.map(reason => reason(feedItem, url.toExternalForm))).map { possibleObjections =>
        possibleObjections.flatten
      }

    }.getOrElse {
      Future.successful(Seq("Invalid URL: " + feedItem.url))
    }
  }

}
