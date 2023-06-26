package nz.co.searchwellington.feeds.suggesteditems

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.FeedItemActionDecorator
import nz.co.searchwellington.feeds.whakaoko.WhakaokoService
import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.frontend.{FrontendFeedItem, FrontendResource}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, User}
import nz.co.searchwellington.repositories.SuppressionDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class SuggestedFeeditemsService @Autowired()(feedItemActionDecorator: FeedItemActionDecorator,
                                                        mongoRepository: MongoRepository,
                                                        whakaokoService: WhakaokoService,
                                                        frontendResourceMapper: FrontendResourceMapper,
                                                        suppressionDAO: SuppressionDAO) extends
  ReasonableWaits {

  private val log = LogFactory.getLog(classOf[SuggestedFeeditemsService])

  private val MaximumChannelPagesToScan = 5

  /*
    Return a list of suggested feed newsitems which an editor may wish to accept after reviewing them.
    These are the latest contents of feeds with SUGGESTED acceptances policy.
    Items which have already been accepted or have suppressed URLs should be excluded.
    This means we might need to paginate to fill our requested items count.
   */
  def getSuggestionFeednewsitems(maxItems: Int, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Seq[FrontendResource]] = {

    def paginateChannelFeedItems(page: Int = 1, output: Seq[(FeedItem, Feed)] = Seq.empty, feeds: Seq[Feed]): Future[Seq[(FeedItem, Feed)]] = {
      log.info("Fetching filter page: " + page + "/" + output.size)
      getChannelFeedItemsDecoratedWithFeeds(page, feeds).flatMap { channelFeedItems =>
        log.info("Found " + channelFeedItems.size + " channel newsitems on page " + page)

        val suggestedChannelNewsitems: Seq[(FeedItem, Feed)] = channelFeedItems

        val eventuallyFiltered = suggestedChannelNewsitems.map { tuple =>
          val eventuallyLocalCopy = mongoRepository.getResourceByUrl(tuple._1.url)
          val eventuallyIsSuppressed = suppressionDAO.isSupressed(tuple._1.url)
          for {
            localCopy <- eventuallyLocalCopy
            isSuppressed <- eventuallyIsSuppressed
          } yield {
            if (localCopy.nonEmpty || isSuppressed) {
              None
            } else {
              Some(tuple)
            }
          }
        }

        Future.sequence(eventuallyFiltered).flatMap { filterResults =>
          val suggestions = filterResults.flatten
          log.info("Adding " + suggestions.size + " to " + output.size)
          val result = output ++ suggestions
          if (result.size >= maxItems || channelFeedItems.isEmpty || page == MaximumChannelPagesToScan) {
            Future.successful(result.take(maxItems))
          } else {
            paginateChannelFeedItems(page + 1, result, feeds)
          }
        }
      }
    }

    for {
      feeds <- mongoRepository.getAllFeeds()
      suggestedFeeds = feeds.filter(feed => feed.acceptance == FeedAcceptancePolicy.SUGGEST)
      suggestedFeedItems: Seq[(FeedItem, Feed)] <- paginateChannelFeedItems(feeds = suggestedFeeds)
      frontendFeedItems: Seq[(FrontendFeedItem, Feed)] <-{
        Future.sequence {
          suggestedFeedItems.map { tuple =>
            val (feedItem, feed) = tuple
            val eventualFrontendFeedItem = frontendResourceMapper.mapFeedItem(feed, feedItem)
            eventualFrontendFeedItem.map { frontendFeedItem =>
              (frontendFeedItem, feed)
            }
          }
        }
      }
      withActions: Seq[FrontendResource] <- Future.sequence {
        frontendFeedItems.map { tuple =>
          val (feedItem, feed) = tuple
          feedItemActionDecorator.withFeedItemSpecificActions(feed, feedItem, loggedInUser)
        }
      }

    } yield {
      withActions
    }
  }

  // Appears to be enhancing the getChannelFeedItems call by decorating the feed items with the feed
  private def getChannelFeedItemsDecoratedWithFeeds(page: Int, feeds: Seq[Feed])(implicit ec: ExecutionContext, currentSpan: Span): Future[Seq[(FeedItem, Feed)]] = {
    def decorateFeedItemsWithFeeds(feedItems: Seq[FeedItem], feeds: Seq[Feed]): Seq[(FeedItem, Feed)] = {
      val subscriptionsToFeeds = feeds.flatMap { feed =>
        feed.whakaokoSubscription.map { subscriptionId =>
          (subscriptionId, feed)
        }
      }.toMap

      feedItems.flatMap { fi =>
        subscriptionsToFeeds.get(fi.subscriptionId).map { feed =>
          (fi, feed)
        }
      }
    }

    for {
      channelFeedItems <- whakaokoService.getChannelFeedItems(page, Some(feeds.flatMap(_.whakaokoSubscription)))
    } yield {
      decorateFeedItemsWithFeeds(channelFeedItems.getOrElse(Seq.empty), feeds)
    }
  }

}
