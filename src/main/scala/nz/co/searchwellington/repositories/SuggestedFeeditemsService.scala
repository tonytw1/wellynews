package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, FeeditemToNewsitemService, RssfeedNewsitemService}
import nz.co.searchwellington.model.frontend.{FeedNewsitemForAcceptance, FrontendNewsitem, FrontendResource}
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

@Component class SuggestedFeeditemsService @Autowired()(rssfeedNewsitemService: RssfeedNewsitemService,
                                                        feedItemLocalCopyDecorator: FeedItemLocalCopyDecorator,
                                                        feeditemToNewsitemService: FeeditemToNewsitemService) extends
  ReasonableWaits {

  def getSuggestionFeednewsitems(maxItems: Int): Future[Seq[FrontendNewsitem]] = {
    rssfeedNewsitemService.getChannelFeedItems.map { channelFeedItems =>
      val notIgnoredFeedItems = channelFeedItems.filter(i => isNotIgnored(i._1, i._2))

      val channelNewsitems = notIgnoredFeedItems.map(i => feeditemToNewsitemService.makeNewsitemFromFeedItem(i._1, i._2))

      val suggestions = feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(channelNewsitems)

      val withLocalCopiesFilteredOut = suggestions.filter(havingNoLocalCopy)
      withLocalCopiesFilteredOut.map(_.newsitem)
    }
  }

  private def isNotIgnored(feedItem: FeedItem, feed: Feed): Boolean = {
    feed.acceptance != FeedAcceptancePolicy.IGNORE
  }

  private def havingNoLocalCopy(feedItem: FeedNewsitemForAcceptance): Boolean = {
    feedItem.localCopy.isEmpty
  }

}
