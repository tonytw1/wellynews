package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, FeeditemToNewsitemService, RssfeedNewsitemService}
import nz.co.searchwellington.model.frontend.{FeedNewsitemForAcceptance, FrontendNewsitem, FrontendResource}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, Newsitem, User}
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

  def getSuggestionFeednewsitems(maxItems: Int, loggedInUser: Option[User])(implicit ec: ExecutionContext): Future[Seq[FrontendResource]] = {

    def isNotIgnored(feedItem: FeedItem, feed: Feed): Boolean = feed.acceptance != FeedAcceptancePolicy.IGNORE

    def havingNoLocalCopy(feedItem: FrontendResource): Boolean = false // TODO reimplement feedItem.localCopy.isEmpty

    def filteredPage(page: Int, output: Seq[FrontendResource]): Future[Seq[FrontendResource]] = {
      log.info("Fetching filter page: " + page + "/" + output.size)
      rssfeedNewsitemService.getChannelFeedItems(page).flatMap { channelFeedItems =>
        log.info("Found " + channelFeedItems.size + " channel newsitems on page " + page)

        val notIgnoredFeedItems = channelFeedItems.filter(i => isNotIgnored(i._1, i._2))
        log.info("After filtering out those from ignored feeds: " + notIgnoredFeedItems.size)

        val channelNewsitems: Seq[Newsitem] = notIgnoredFeedItems.map(i => feeditemToNewsitemService.makeNewsitemFromFeedItem(i._1, i._2))

        val eventualFrontendChannelResources = Future.sequence {
          channelNewsitems.map { r =>
            frontendResourceMapper.createFrontendResourceFrom(r, loggedInUser)
          }
        }

        eventualFrontendChannelResources.flatMap { rs =>
          feedItemLocalCopyDecorator.withFeedItemSpecificActions(rs, loggedInUser).flatMap { suggestions =>
            val withLocalCopiesFilteredOut: Seq[FrontendResource] = suggestions.filter(havingNoLocalCopy)
            log.info("After filtering out those with local copies: " + withLocalCopiesFilteredOut.size)

            val filteredNewsitems = withLocalCopiesFilteredOut // .map(_.newsitem)
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
    }

    filteredPage(1, Seq.empty)
  }

}
