package nz.co.searchwellington.feeds

import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.FeedAcceptancePolicy
import nz.co.searchwellington.repositories.SuppressionDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class FeedItemAcceptanceDecider @Autowired()(mongoRepository: MongoRepository, suppressionDAO: SuppressionDAO) {

  private val log = Logger.getLogger(classOf[FeedItemAcceptanceDecider])

  def getAcceptanceErrors(feeditem: FeedItem, acceptancePolicy: FeedAcceptancePolicy)(implicit ec: ExecutionContext): Future[Seq[String]] = {
    val cannotBeSuppressed: FeedItem => Future[Option[String]] = (feedItem: FeedItem) => {
      suppressionDAO.isSupressed(feedItem.url).map { isSuppressed =>
        log.debug("Is feed item url '" + feedItem.url + "' suppressed: " + isSuppressed)
        if (isSuppressed) Some("This item is suppressed") else None
      }
    }

    val titleCannotBeBlank = (feedItem: FeedItem) => {
      Future.successful {
        if (feedItem.title.getOrElse("").trim.isEmpty) {
          Some("Item has no title")
        } else {
          None
        }
      }
    }

    val cannotBeMoreThanOneWeekOld = (feedItem: FeedItem) => {
      Future.successful {
        if (acceptancePolicy == FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES) {
          None

        } else {
          if (feedItem.date.isEmpty) {
            Some("Item has no date and feed acceptance policy is not accept even without dates")

          } else {
            feeditem.date.flatMap { date =>
              val oneWeekAgo = DateTime.now.minusWeeks(1)
              val isMoreThanOneWeekOld = date.isBefore(oneWeekAgo)
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

    val cannotHaveDateInTheFuture = (_: FeedItem) => Future.successful(None) // TODO

    val cannotAlreadyHaveThisFeedItem = (feedItem: FeedItem) => {
      val eventualAlreadyHaveThisFeedItem = mongoRepository.getResourceByUrl(feedItem.url).map(_.nonEmpty)
      eventualAlreadyHaveThisFeedItem.map { alreadyHaveThisFeedItem =>
        if (alreadyHaveThisFeedItem) {
          log.debug("A resource with url '" + feeditem.url + "' already exists; not accepting.")
          Some("Item already exists")
        } else {
          None
        }
      }
    }

    val alreadyHaveAnItemWithTheSameHeadlineFromTheSamePublisherWithinTheLastMonth = (_: FeedItem) => {
      Future.successful(None)
    } // TODO implement me


    val reasonsToRejectFeedItems = Seq(
      cannotAlreadyHaveThisFeedItem,
      cannotBeSuppressed,
      titleCannotBeBlank,
      cannotBeMoreThanOneWeekOld,
      cannotHaveDateInTheFuture,
      alreadyHaveAnItemWithTheSameHeadlineFromTheSamePublisherWithinTheLastMonth)

    Future.sequence(reasonsToRejectFeedItems.map(reason => reason(feeditem))).map { possibleObjections =>
      possibleObjections.flatten
    }
  }

}
