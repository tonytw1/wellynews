package nz.co.searchwellington.feeds

import nz.co.searchwellington.feeds.reading.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy}
import nz.co.searchwellington.repositories.SuppressionDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.utils.UrlCleaner
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class FeedAcceptanceDecider @Autowired()(mongoRepository: MongoRepository,
                                                    suppressionDAO: SuppressionDAO,
                                                    urlCleaner: UrlCleaner) {

  private val log = Logger.getLogger(classOf[FeedAcceptanceDecider])

  def getAcceptanceErrors(feed: Feed, feedNewsitem: FeedItem, acceptancePolicy: FeedAcceptancePolicy): Future[Seq[String]] = {
    val cleanedUrl = urlCleaner.cleanSubmittedItemUrl(feedNewsitem.url) // TODO duplication

    def cannotBeSupressed(): Future[Option[String]] = {
      suppressionDAO.isSupressed(cleanedUrl).map { isSuppressed =>
        log.debug("Is feed item url '" + cleanedUrl + "' suppressed: " + isSuppressed)
        if (isSuppressed) Some("This item is suppressed") else None
      }
    }

    def titleCannotBeBlank(): Future[Option[String]] = {
      Future.successful {
        if (feedNewsitem.title.getOrElse("").trim.isEmpty) {
          Some("Item has no title")
        } else {
          None
        }
      }
    }

    def cannotBeMoreThanOneWeekOld(): Future[Option[String]] = {
      Future.successful {
        if (acceptancePolicy == FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES) {
          None

        } else {

          if (feed.date.isEmpty) {
            Some("Item has no date and feed acceptance policy is not accept even without dates")

          } else {

            feedNewsitem.date.flatMap { date =>
              val oneWeekAgo = DateTime.now.minusWeeks(1)
              val isMoreThanOneWeekOld = new DateTime(feedNewsitem.date).isBefore(oneWeekAgo)
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

    def cannotHaveDateInTheFuture(): Future[Option[String]] = Future.successful(None) // TODO

    def cannotAlreadyHaveThisFeedItem(): Future[Option[String]] = {
      alreadyHaveThisFeedItem(feedNewsitem).map { alreadyHaveThisFeedItem =>
        if (alreadyHaveThisFeedItem) {
          log.debug("A resource with url '" + feedNewsitem.url + "' already exists; not accepting.")
          Some("Item already exists")
        } else {
          None
        }
      }
    }

    def alreadyHaveAnItemWithTheSameHeadlineFromTheSamePublisherWithinTheLastMonth(): Future[Option[String]] = {
      Future.successful(None)
    } // TODO implement me


    val eventualObjections: Future[Seq[Option[String]]] = Future.sequence {
      Seq(
        cannotBeSupressed(),
        titleCannotBeBlank(),
        // cannotBeMoreThanOneWeekOld(),  TODO reinstate
        cannotHaveDateInTheFuture(),
        cannotAlreadyHaveThisFeedItem(),
        alreadyHaveAnItemWithTheSameHeadlineFromTheSamePublisherWithinTheLastMonth()
      )
    }

    eventualObjections.map { objections =>
      objections.flatten
    }
  }

  private def alreadyHaveThisFeedItem(feedNewsitem: FeedItem): Future[Boolean] = {
    val url = urlCleaner.cleanSubmittedItemUrl(feedNewsitem.url)
    mongoRepository.getResourceByUrl(url).map(_.nonEmpty)
  }

}