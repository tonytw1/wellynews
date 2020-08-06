package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, FeeditemToNewsitemService, RssfeedNewsitemService}
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class SuggestedFeeditemsService @Autowired()(rssfeedNewsitemService: RssfeedNewsitemService,
                                                        feedItemLocalCopyDecorator: FeedItemLocalCopyDecorator,
                                                        feeditemToNewsitemService: FeeditemToNewsitemService,
                                                        frontendResourceMapper: FrontendResourceMapper) extends
  ReasonableWaits {

  private val log = Logger.getLogger(classOf[SuggestedFeeditemsService])

  private val MaximumChannelPagesToScan = 5

  def getSuggestionFeednewsitems(maxItems: Int, loggedInUser: Option[User])(implicit ec: ExecutionContext): Future[Seq[FrontendResource]] = {

    def canBeAccepted(feedItem: FrontendResource): Boolean = feedItem.actions.exists(a => a.label == "Accept")  // TODO matching on action label is not great

    def filteredPage(page: Int, output: Seq[FrontendResource]): Future[Seq[FrontendResource]] = {
      log.info("Fetching filter page: " + page + "/" + output.size)
      rssfeedNewsitemService.getChannelFeedItems(page).flatMap { channelFeedItems =>
        log.info("Found " + channelFeedItems.size + " channel newsitems on page " + page)
        val channelNewsitems = channelFeedItems.map(i => feeditemToNewsitemService.makeNewsitemFromFeedItem(i._1, i._2))
        val eventualFrontendChannelResources = Future.sequence {
          channelNewsitems.map { r =>
            frontendResourceMapper.mapFrontendResource(r, r.geocode)
          }
        }

        eventualFrontendChannelResources.flatMap { rs =>
          feedItemLocalCopyDecorator.withFeedItemSpecificActions(rs, loggedInUser).flatMap { suggestions =>
            val withLocalCopiesFilteredOut = suggestions.filter(canBeAccepted)
            log.info("After filtering out those with local copies: " + withLocalCopiesFilteredOut.size)

            log.info("Adding " + withLocalCopiesFilteredOut.size + " to " + output.size)
            val result = output ++ withLocalCopiesFilteredOut
            if (result.size >= maxItems || channelFeedItems.isEmpty || page == MaximumChannelPagesToScan) {
              Future.successful(result.take(maxItems))
            } else {
              filteredPage(page + 1, result)
            }
          }
        }
      }
    }

    filteredPage(1, Seq.empty)
  }

}
