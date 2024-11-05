package nz.co.searchwellington.feeds.reading

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.feeds.{FeedItemAcceptanceDecider, FeedReaderUpdateService}
import nz.co.searchwellington.model.{Feed, Newsitem, User}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Component
class KafkaFeed(mongoRepository: MongoRepository,
                feedItemAcceptanceDecider: FeedItemAcceptanceDecider,
                feedReaderUpdateService: FeedReaderUpdateService) extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[KafkaFeed])

  private val FEED_READER_PROFILE_NAME = "feedreader" // TODO duplication

  @KafkaListener(topics = Array("whakaoko.wellynews"))
  def listen(message: String): Unit = {
    log.info(s"Got message: " + message)

    // Should be able to deserialize this as feed item
    try {
      val feedItem = Json.parse(message).as[FeedItem]
      log.info(s"Parsed feed item from message: $feedItem")

      val eventualMaybeFeed = mongoRepository.getFeedByWhakaokoSubscriptionId(feedItem.subscriptionId)
      val eventualMaybeUser = mongoRepository.getUserByProfilename(FEED_READER_PROFILE_NAME)

      val x = for {
        maybeFeed: Option[Feed] <- eventualMaybeFeed
        maybeFeedReaderUser: Option[User] <- eventualMaybeUser
      } yield {

        val z = for {
          feed <- maybeFeed
          feedReaderUser <- maybeFeedReaderUser
        } yield {
          feedItemAcceptanceDecider.getAcceptanceErrors(feedItem, feed.getAcceptancePolicy).flatMap { acceptanceErrors =>
            if (feed.acceptance.shouldAcceptFeedItems() && acceptanceErrors.isEmpty) {
              feedReaderUpdateService.acceptFeeditem(feedReaderUser, feedItem, feed, feedItem.categories.getOrElse(Seq.empty)).map { acceptedNewsitem =>
                log.info("Feed item accepted as news item: " + acceptedNewsitem)
                acceptedNewsitem
              }.recover {
                case e: Exception =>
                  log.error("Error while accepting feed item", e)
                  None
              }
            } else {
              Future.successful(None)
            }
          }
        }
        z.getOrElse(Future.successful(None))
      }
      val flatten: Future[Option[Newsitem]] = x.flatten

      val maybeNewsitem = Await.result(flatten, TenSeconds)
      log.info("Message mapped to accepted newsitem: " + maybeNewsitem)

    } catch {
      case e: Exception =>
        log.warn(s"Failed to parse feed item message: ${e.getMessage} / $message")
    }
  }
}
