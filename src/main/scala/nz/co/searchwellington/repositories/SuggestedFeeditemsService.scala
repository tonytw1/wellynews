package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, FeeditemToNewsitemService, RssfeedNewsitemService}
import nz.co.searchwellington.model.frontend.{FeedNewsitemForAcceptance, FrontendNewsitem}
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class SuggestedFeeditemsService @Autowired()(rssfeedNewsitemService: RssfeedNewsitemService,
                                                        feedItemLocalCopyDecorator: FeedItemLocalCopyDecorator,
                                                        feeditemToNewsitemService: FeeditemToNewsitemService) extends
  ReasonableWaits {

  def getSuggestionFeednewsitems(maxItems: Int): Future[Seq[FrontendNewsitem]] = {

    def isNotIgnored(feedItem: FeedItem, feed: Feed): Boolean = {
      feed.acceptance != FeedAcceptancePolicy.IGNORE
    }

    def havingNoLocalCopy(feedItem: FeedNewsitemForAcceptance): Boolean = {
      feedItem.localCopy.isEmpty
    }

    rssfeedNewsitemService.getChannelFeedItems.flatMap { channelFeedItems =>
      val notIgnoredFeedItems = channelFeedItems.filter(i => isNotIgnored(i._1, i._2))

      val channelNewsitems = notIgnoredFeedItems.map(i => feeditemToNewsitemService.makeNewsitemFromFeedItem(i._1, i._2))

      feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(channelNewsitems).map { suggestions =>
        val withLocalCopiesFilteredOut = suggestions.filter(havingNoLocalCopy)
        withLocalCopiesFilteredOut.map(_.newsitem)
      }
    }
  }

}
