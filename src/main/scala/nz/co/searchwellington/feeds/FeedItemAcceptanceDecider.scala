package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.{FeedAcceptancePolicy, Newsitem}
import nz.co.searchwellington.repositories.SuppressionDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class FeedItemAcceptanceDecider @Autowired()(mongoRepository: MongoRepository, suppressionDAO: SuppressionDAO) {

  private val log = Logger.getLogger(classOf[FeedItemAcceptanceDecider])

  def getAcceptanceErrors(newsitem: Newsitem, acceptancePolicy: FeedAcceptancePolicy)(implicit ec: ExecutionContext): Future[Seq[String]] = {
    val cannotBeSuppressed: Newsitem => Future[Option[String]] = (newsitem: Newsitem) => {
      suppressionDAO.isSupressed(newsitem.page).map { isSuppressed =>
        log.debug("Is feed item url '" + newsitem.page + "' suppressed: " + isSuppressed)
        if (isSuppressed) Some("This item is suppressed") else None
      }
    }

    val titleCannotBeBlank = (newsitem: Newsitem) => {
      Future.successful {
        if (newsitem.title.getOrElse("").trim.isEmpty) {
          Some("Item has no title")
        } else {
          None
        }
      }
    }

    val cannotBeMoreThanOneWeekOld = (newsitem: Newsitem) => {
      Future.successful {
        if (acceptancePolicy == FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES) {
          None

        } else {
          if (newsitem.date.isEmpty) {
            Some("Item has no date and feed acceptance policy is not accept even without dates")

          } else {
            newsitem.date.flatMap { date =>
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

    val cannotHaveDateInTheFuture = (_: Newsitem) => Future.successful(None) // TODO

    val cannotAlreadyHaveThisFeedItem = (newsitem: Newsitem) => {
      val eventualAlreadyHaveThisFeedItem = mongoRepository.getResourceByUrl(newsitem.page).map(_.nonEmpty)
      eventualAlreadyHaveThisFeedItem.map { alreadyHaveThisFeedItem =>
        if (alreadyHaveThisFeedItem) {
          log.debug("A resource with url '" + newsitem.page + "' already exists; not accepting.")
          Some("Item already exists")
        } else {
          None
        }
      }
    }

    val alreadyHaveAnItemWithTheSameHeadlineFromTheSamePublisherWithinTheLastMonth = (_: Newsitem) => {
      Future.successful(None)
    } // TODO implement me


    val reasonsToRejectFeedItems = Seq(
      cannotAlreadyHaveThisFeedItem,
      cannotBeSuppressed,
      titleCannotBeBlank,
      cannotBeMoreThanOneWeekOld,
      cannotHaveDateInTheFuture,
      alreadyHaveAnItemWithTheSameHeadlineFromTheSamePublisherWithinTheLastMonth)

    Future.sequence(reasonsToRejectFeedItems.map(reason => reason(newsitem))).map { possibleObjections =>
      possibleObjections.flatten
    }
  }

}
