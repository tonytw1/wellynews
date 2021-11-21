package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.WhakaokoService
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy}
import nz.co.searchwellington.model.frontend.FrontendFeed
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{Await, ExecutionContext, Future}

@Component
class InboxFeedsService @Autowired()(mongoRepository: MongoRepository, whakaokoService: WhakaokoService, frontendResourceMapper: FrontendResourceMapper)
  extends ReasonableWaits {

  def getInboxFeeds()(implicit ec: ExecutionContext): Future[Seq[FrontendFeed]] = {
    val eventualWhakaokoSubscriptions = whakaokoService.getSubscriptions
    for {
      allFeeds: Seq[Feed] <- mongoRepository.getAllFeeds
      suggestOnlyFeeds = allFeeds.filter(_.getAcceptancePolicy == FeedAcceptancePolicy.SUGGEST)
      whakaokoSubscriptions <- eventualWhakaokoSubscriptions
      inboxFeeds <- {
        val subscriptionsById = whakaokoSubscriptions.map { subscription =>
          (subscription.id, subscription)
        }.toMap

        val feedsWithSubscriptions = suggestOnlyFeeds.flatMap { feed =>
          val mayBeSubscription = feed.whakaokoSubscription.map { subscriptionId =>
            subscriptionsById(subscriptionId)
          }
          mayBeSubscription.map { subscription =>
            Some(feed, subscription)
          }
        }.flatten

        // Override the last change field with better information from the whakaoko subscription; TODO this field is really last accepted date or something
        val frontendFeedsWithAmmendedLatestItemDates = feedsWithSubscriptions.map { feed =>
          frontendResourceMapper.createFrontendResourceFrom(feed._1).map {
            case frontendFeed: FrontendFeed =>
              val subscription = feed._2
              Some(frontendFeed.copy(latestItemDate = subscription.latestItemDate.map(_.toDate).orNull))
            case _ =>
              None
          }
        }

        Future.sequence(frontendFeedsWithAmmendedLatestItemDates).map { maybeFrontendFeeds =>
          maybeFrontendFeeds.flatten.filter(_.latestItemDate != null).sortBy(_.latestItemDate).reverse
        }
      }

    } yield {
      inboxFeeds
    }
  }

}
