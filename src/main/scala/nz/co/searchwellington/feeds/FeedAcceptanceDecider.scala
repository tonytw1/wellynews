package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy}
import nz.co.searchwellington.repositories.{HibernateResourceDAO, SupressionDAO}
import nz.co.searchwellington.utils.UrlCleaner
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class FeedAcceptanceDecider @Autowired()(resourceDAO: HibernateResourceDAO,
                                                    supressionDAO: SupressionDAO,
                                                    urlCleaner: UrlCleaner) {

  private val log = Logger.getLogger(classOf[FeedAcceptanceDecider])

  def getAcceptanceErrors(feed: Feed, feedNewsitem: FrontendFeedNewsitem, acceptancePolicy: FeedAcceptancePolicy): Seq[String] = {
    val cleanedUrl = urlCleaner.cleanSubmittedItemUrl(feedNewsitem.getUrl)
    val isSuppressed = supressionDAO.isSupressed(cleanedUrl)

    def cannotBeSupressed(): Option[String] = {
      log.debug("Is feed item url '" + cleanedUrl + "' supressed: " + isSuppressed)
      if (isSuppressed) Some("This item is supressed") else None
    }

    def titleCannotBeBlank(): Option[String] = {
      if (feedNewsitem.getName != null && feedNewsitem.getName.trim.isEmpty) {
        Some("Item has no title")
      } else {
        None
      }
    }

    def cannotBeMoreThanOneWeekOld(): Option[String] = {
      if (acceptancePolicy == FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES) {
        None
      } else {
        if (feedNewsitem.getDate == null) {
          Some("Item has no date and feed acceptance policy is not accept even without dates")

        } else {
          val oneWeekAgo = DateTime.now.minusWeeks(1)
          val isMoreThanOneWeekOld = new DateTime(feedNewsitem.getDate).isBefore(oneWeekAgo)
          if (isMoreThanOneWeekOld) {
            Some("Item is more than one week old")
          } else {
            None
          }
        }
      }
    }

    def cannotHaveDateInTheFuture(): Option[String] = {
      None
    }

    def cannotAlreadyHaveThisFeedItem(): Option[String] = {
      if (alreadyHaveThisFeedItem(feedNewsitem)) {
        log.debug("A resource with url '" + feedNewsitem.getUrl + "' already exists; not accepting.")
        Some("Item already exists")
      } else {
        None
      }
    }

    def cannotImportIfAlreadyExists(): Option[String] = {
      if (alreadyHaveThisFeedItem(feedNewsitem)) {
        log.debug("A resource with url '" + feedNewsitem.getUrl + "' already exists; not accepting.")
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
      cannotBeMoreThanOneWeekOld(),
      cannotHaveDateInTheFuture(),
      cannotAlreadyHaveThisFeedItem(),
      alreadyHaveAnItemWithTheSameHeadlineFromTheSamePublisherWithinTheLastMonth()
    ).flatten
  }

  def shouldSuggest(feednewsitem: FrontendFeedNewsitem): Boolean = {
    !alreadyHaveThisFeedItem(feednewsitem)
  }

  private def alreadyHaveThisFeedItem(feedNewsitem: FrontendFeedNewsitem): Boolean = {
    val url = urlCleaner.cleanSubmittedItemUrl(feedNewsitem.getUrl)
    resourceDAO.loadResourceByUrl(url) != null
  }

}