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

    def filteredPage(page: Int, output: Seq[FrontendNewsitem]): Future[Seq[FrontendNewsitem]] = {
      log.info("Fetching filter page: " + page + "/" + output.size)
      rssfeedNewsitemService.getChannelFeedItems(page = 1).flatMap { channelFeedItems =>
        log.info("Found " + channelFeedItems.size + " channel newsitems on page " + page)

        val notIgnoredFeedItems = channelFeedItems.filter(i => isNotIgnored(i._1, i._2))
        log.info("After filtering out those from ignored feeds: " + notIgnoredFeedItems.size)

        val channelNewsitems = notIgnoredFeedItems.map(i => feeditemToNewsitemService.makeNewsitemFromFeedItem(i._1, i._2))

        feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(channelNewsitems).flatMap { suggestions =>
          val withLocalCopiesFilteredOut = suggestions.filter(havingNoLocalCopy)
          log.info("After filtering out those with local copies: " + withLocalCopiesFilteredOut.size)

          val filteredNewsitems = withLocalCopiesFilteredOut.map(_.newsitem)
          log.info("Adding " + filteredNewsitems.size + " to " + output.size)
          val result = output ++ filteredNewsitems
          if (result.size >= maxItems || channelFeedItems.isEmpty || page == 5) {
            Future.successful(result.take(maxItems))
          } else {
            filteredPage(page + 1, result)
          }
        }
      }
    }

    filteredPage(1, Seq.empty)
  }

}
