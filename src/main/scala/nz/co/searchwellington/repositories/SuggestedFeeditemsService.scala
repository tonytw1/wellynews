package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.models.helpers.FeedsModelBuilder
import nz.co.searchwellington.feeds.reading.whakaoko.model.FeedItem
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, FeeditemToNewsitemService, RssfeedNewsitemService}
import nz.co.searchwellington.model.frontend.{FeedNewsitemForAcceptance, FrontendNewsitem}
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class SuggestedFeeditemsService @Autowired()(rssfeedNewsitemService: RssfeedNewsitemService,
                                                        feedItemLocalCopyDecorator: FeedItemLocalCopyDecorator,
                                                        feeditemToNewsitemService: FeeditemToNewsitemService) extends
  ReasonableWaits {

  private val log = Logger.getLogger(classOf[FeedsModelBuilder])

  def getSuggestionFeednewsitems(maxItems: Int): Future[Seq[FrontendNewsitem]] = {

    def isNotIgnored(feedItem: FeedItem, feed: Feed): Boolean = feed.acceptance != FeedAcceptancePolicy.IGNORE


    def havingNoLocalCopy(feedItem: FeedNewsitemForAcceptance): Boolean = feedItem.localCopy.isEmpty

    rssfeedNewsitemService.getChannelFeedItems.flatMap { channelFeedItems =>
      log.info("Found " + channelFeedItems.size + " channel newsitems")

      val notIgnoredFeedItems = channelFeedItems.filter(i => isNotIgnored(i._1, i._2))
      log.info("After filtering out those from ignored feeds: " + notIgnoredFeedItems.size)

      val channelNewsitems = notIgnoredFeedItems.map(i => feeditemToNewsitemService.makeNewsitemFromFeedItem(i._1, i._2))

      feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(channelNewsitems).map { suggestions =>
        val withLocalCopiesFilteredOut = suggestions.filter(havingNoLocalCopy)
        log.info("After filtering out those with local copies: " + withLocalCopiesFilteredOut.size)
        withLocalCopiesFilteredOut.map(_.newsitem).take(maxItems)
      }
    }
  }

}
