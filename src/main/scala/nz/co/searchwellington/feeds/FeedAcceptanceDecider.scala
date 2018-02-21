package nz.co.searchwellington.feeds

import java.io.StringWriter
import java.util.Calendar

import com.google.common.collect.Lists
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy}
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem
import nz.co.searchwellington.repositories.{HibernateResourceDAO, SupressionDAO}
import nz.co.searchwellington.utils.UrlCleaner
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class FeedAcceptanceDecider @Autowired() (resourceDAO: HibernateResourceDAO,
  supressionDAO: SupressionDAO,
  urlCleaner: UrlCleaner) {

  private val log = Logger.getLogger(classOf[FeedAcceptanceDecider])

  def getAcceptanceErrors(feed: Feed, feedNewsitem: FrontendFeedNewsitem, acceptancePolicy: FeedAcceptancePolicy): java.util.List[String] = {
    val acceptanceErrors: java.util.List[String] = Lists.newArrayList()
    val cleanedUrl = urlCleaner.cleanSubmittedItemUrl(feedNewsitem.getUrl)
    val isSuppressed: Boolean = supressionDAO.isSupressed(cleanedUrl)
    log.debug("Is feed item url '" + cleanedUrl + "' supressed: " + isSuppressed)
    if (isSuppressed) {
      acceptanceErrors.add("This item is supressed")
    }
    val titleIsBlank: Boolean = feedNewsitem.getName != null && feedNewsitem.getName == ""
    if (titleIsBlank) {
      acceptanceErrors.add("Item has no title")
    }
    lessThanOneWeekOld(feedNewsitem, acceptancePolicy, acceptanceErrors)
    hasDateInTheFuture(feedNewsitem, acceptanceErrors)
    alreadyHaveThisFeedItem(feedNewsitem, acceptanceErrors)
    alreadyHaveAnItemWithTheSameHeadlineFromTheSamePublisherWithinTheLastMonth(feedNewsitem, acceptanceErrors, feed)
    return acceptanceErrors
  }

  def shouldSuggest(feednewsitem: FrontendFeedNewsitem): Boolean = {
    val cleanSubmittedItemUrl: String = urlCleaner.cleanSubmittedItemUrl(feednewsitem.getUrl)
    val isSuppressed: Boolean = supressionDAO.isSupressed(cleanSubmittedItemUrl)
    if (isSuppressed) {
      return false
    }
    val acceptanceErrors: java.util.List[String] = Lists.newArrayList()
    alreadyHaveThisFeedItem(feednewsitem, acceptanceErrors)
    return acceptanceErrors.isEmpty
  }

  private def hasDateInTheFuture(resource: FrontendFeedNewsitem, acceptanceErrors: java.util.List[String]) {
    val oneDayFromNow: Calendar = Calendar.getInstance
    oneDayFromNow.add(Calendar.DATE, 1)
    if (resource.getDate != null && resource.getDate.after(oneDayFromNow.getTime)) {
      val message: StringWriter = new StringWriter
      message.append("Has date in the future")
      message.append(" (" + resource.getDate.toString + " is after " + oneDayFromNow.getTime.toString + ")")
      acceptanceErrors.add(message.toString)
    }
  }

  private def alreadyHaveThisFeedItem(resourceFromFeed: FrontendFeedNewsitem, acceptanceErrors: java.util.List[String]) {
    val url = urlCleaner.cleanSubmittedItemUrl(resourceFromFeed.getUrl)
    if (resourceDAO.loadResourceByUrl(url) != null) {
      log.debug("A resource with url '" + resourceFromFeed.getUrl + "' already exists; not accepting.")
      acceptanceErrors.add("Item already exists")
    }
  }

  private def alreadyHaveAnItemWithTheSameHeadlineFromTheSamePublisherWithinTheLastMonth(resource: FrontendFeedNewsitem, acceptanceErrors: java.util.List[String], feed: Feed) {
  }

  private def lessThanOneWeekOld(feedNewsitem: FrontendFeedNewsitem, acceptancePolicy: FeedAcceptancePolicy, acceptanceErrors: java.util.List[String]) {
    if (acceptancePolicy == FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES) {
      return
    }
    if (feedNewsitem.getDate == null) {
      acceptanceErrors.add("Item has no date and feed acceptance policy is not accept even without dates")
      return
    }
    val oneWeekAgo: DateTime = DateTime.now.minusWeeks(1)
    val isMoreThanOneWeekOld: Boolean = new DateTime(feedNewsitem.getDate).isBefore(oneWeekAgo)
    if (isMoreThanOneWeekOld) {
      acceptanceErrors.add("Item is more than one week old")
    }
  }

}