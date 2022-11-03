package nz.co.searchwellington.feeds

import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.FeedAcceptancePolicy
import nz.co.searchwellington.repositories.SuppressionDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class FeedItemAcceptanceDecider @Autowired()(mongoRepository: MongoRepository, suppressionDAO: SuppressionDAO) {

  private val log = LogFactory.getLog(classOf[FeedItemAcceptanceDecider])

  def getAcceptanceErrors(feedItem: FeedItem, acceptancePolicy: FeedAcceptancePolicy)(implicit ec: ExecutionContext): Future[Seq[String]] = {
    val cannotBeSuppressed: FeedItem => Future[Option[String]] = (feedItem: FeedItem) => {
      suppressionDAO.isSupressed(feedItem.url).map { isSuppressed =>
        log.debug("Is feed item url '" + feedItem.url + "' suppressed: " + isSuppressed)
        if (isSuppressed) Some("This item is suppressed") else None
      }
    }

    val titleCannotBeBlank = (feedItem: FeedItem) => {
      Future.successful {
        if (feedItem.title.forall(_.trim.isEmpty)) {
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

    val cannotBeMoreThanOneWeekInTheFuture = (feedItem: FeedItem) => {
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

    val cannotAlreadyHaveThisFeedItem = (feedItem: FeedItem) => {
      val eventualAlreadyHaveThisFeedItem = mongoRepository.getResourceByUrl(feedItem.url).map(_.nonEmpty)
      eventualAlreadyHaveThisFeedItem.map { alreadyHaveThisFeedItem =>
        if (alreadyHaveThisFeedItem) {
          log.debug("A resource with url '" + feedItem.url + "' already exists; not accepting.")
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
      cannotBeMoreThanOneWeekInTheFuture,
      alreadyHaveAnItemWithTheSameHeadlineFromTheSamePublisherWithinTheLastMonth)

    Future.sequence(reasonsToRejectFeedItems.map(reason => reason(feedItem))).map { possibleObjections =>
      possibleObjections.flatten
    }
  }

}
