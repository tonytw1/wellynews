package nz.co.searchwellington.feeds.suggesteditems

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.WhakaokoService
import nz.co.searchwellington.model.FeedAcceptancePolicy
import nz.co.searchwellington.model.frontend.FrontendFeed
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component
class SuggestedFeedsService @Autowired()(mongoRepository: MongoRepository, whakaokoService: WhakaokoService, frontendResourceMapper: FrontendResourceMapper)
  extends ReasonableWaits {

  def getSuggestedFeedsOrderedByLatestFeeditemDate()(implicit ec: ExecutionContext): Future[Seq[FrontendFeed]] = {
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
              Some(frontendFeed.copy(latestItemDate = subscription.latestItemDate.map(_.toDate).orNull))
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
