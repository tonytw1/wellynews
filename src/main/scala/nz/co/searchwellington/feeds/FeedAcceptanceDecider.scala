package nz.co.searchwellington.feeds

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.reading.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy}
import nz.co.searchwellington.repositories.SupressionDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.utils.UrlCleaner
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}

@Component class FeedAcceptanceDecider @Autowired()(mongoRepository: MongoRepository,
                                                    supressionDAO: SupressionDAO,
                                                    urlCleaner: UrlCleaner) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[FeedAcceptanceDecider])
  private val tenSeconds = Duration(10, SECONDS)

  def getAcceptanceErrors(feed: Feed, feedNewsitem: FeedItem, acceptancePolicy: FeedAcceptancePolicy): Seq[String] = {
    val cleanedUrl = urlCleaner.cleanSubmittedItemUrl(feedNewsitem.url)  // TODO duplication
    val isSuppressed = Await.result(supressionDAO.isSupressed(cleanedUrl), TenSeconds)

    def cannotBeSupressed(): Option[String] = {
      log.debug("Is feed item url '" + cleanedUrl + "' suppressed: " + isSuppressed)
      if (isSuppressed) Some("This item is supressed") else None
    }

    def titleCannotBeBlank(): Option[String] = {
      if (feedNewsitem.title.getOrElse("").trim.isEmpty) {
        Some("Item has no title")
      } else {
        None
      }
    }

    def cannotBeMoreThanOneWeekOld(): Option[String] = {
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

    def cannotHaveDateInTheFuture(): Option[String] = {
      None
    }

    def cannotAlreadyHaveThisFeedItem(): Option[String] = {
      if (alreadyHaveThisFeedItem(feedNewsitem)) {
        log.debug("A resource with url '" + feedNewsitem.url + "' already exists; not accepting.")
        Some("Item already exists")
      } else {
        None
      }
    }

    def cannotImportIfAlreadyExists(): Option[String] = {
      if (alreadyHaveThisFeedItem(feedNewsitem)) {
        log.debug("A resource with url '" + feedNewsitem.url + "' already exists; not accepting.")
        Some("Item already exists")
      } else {
        None
      }
    }

    def alreadyHaveAnItemWithTheSameHeadlineFromTheSamePublisherWithinTheLastMonth(): Option[String] = {
      None // TODO implement me
    }

    Seq(
      cannotBeSupressed(),
      titleCannotBeBlank(),
      // cannotBeMoreThanOneWeekOld(),  TODO reinstate
      cannotHaveDateInTheFuture(),
      cannotAlreadyHaveThisFeedItem(),
      alreadyHaveAnItemWithTheSameHeadlineFromTheSamePublisherWithinTheLastMonth()
    ).flatten
  }

  def shouldSuggest(feednewsitem: FeedItem): Boolean = {
    !alreadyHaveThisFeedItem(feednewsitem)
  }

  private def alreadyHaveThisFeedItem(feedNewsitem: FeedItem): Boolean = {
    val url = urlCleaner.cleanSubmittedItemUrl(feedNewsitem.url)
    Await.result(mongoRepository.getResourceByUrl(url), tenSeconds).nonEmpty
  }

}