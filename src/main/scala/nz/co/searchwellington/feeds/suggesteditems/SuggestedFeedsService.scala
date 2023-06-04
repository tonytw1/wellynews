package nz.co.searchwellington.feeds.suggesteditems

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.WhakaokoService
import nz.co.searchwellington.model.FeedAcceptancePolicy
import nz.co.searchwellington.model.frontend.FrontendFeed
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component
class SuggestedFeedsService @Autowired()(mongoRepository: MongoRepository, whakaokoService: WhakaokoService, frontendResourceMapper: FrontendResourceMapper)
  extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[SuggestedFeedsService])

  def getSuggestedFeedsOrderedByLatestFeeditemDate()(implicit ec: ExecutionContext, currentSpan: Span): Future[Seq[FrontendFeed]] = {
    val eventualMaybeWhakaokoSubscriptions = whakaokoService.getSubscriptions
    for {
      allFeeds <- mongoRepository.getAllFeeds
      suggestOnlyFeeds = allFeeds.filter(_.getAcceptancePolicy == FeedAcceptancePolicy.SUGGEST)
      maybeWhakaokoSubscriptions <- eventualMaybeWhakaokoSubscriptions
      whakaokoSubscriptions = maybeWhakaokoSubscriptions.toOption.getOrElse(Seq.empty)
      inboxFeeds <- {
        val subscriptionsById = whakaokoSubscriptions.map { subscription =>
          (subscription.id, subscription)
        }.toMap

        val feedsWithSubscriptions = suggestOnlyFeeds.flatMap { feed =>
          for {
            subscriptionId <- feed.whakaokoSubscription
            subscription <- subscriptionsById.get(subscriptionId)
          }  yield {
            (feed, subscription)
          }
        }

        // Override the last change field with better information from the whakaoko subscription; TODO this field is really last accepted date or something
        val frontendFeedsWithAmendedLatestItemDates = feedsWithSubscriptions.map { feed =>
          frontendResourceMapper.createFrontendResourceFrom(feed._1).map {
            case frontendFeed: FrontendFeed =>
              val subscription = feed._2
              val fromSubscription = subscription.latestItemDate.map(_.toDate).orNull

              if (fromSubscription != frontendFeed.latestItemDate) {
                log.info(s"Overriding latest item date for ${frontendFeed.getHeadline} from ${frontendFeed.latestItemDate} to $fromSubscription")
              }

              Some(frontendFeed.copy(latestItemDate = fromSubscription))
            case _ =>
              None
          }
        }

        Future.sequence(frontendFeedsWithAmendedLatestItemDates).map { maybeFrontendFeeds =>
          maybeFrontendFeeds.flatten.filter(_.latestItemDate != null).sortBy(_.latestItemDate).reverse
        }
      }

    } yield {
      inboxFeeds
    }
  }

}
