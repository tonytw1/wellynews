package nz.co.searchwellington.feeds.reading.whakaoko

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import nz.co.searchwellington.feeds.FeedReaderRunner
import nz.co.searchwellington.feeds.reading.whakaoko.model.{FeedItem, LatLong, Place, Subscription}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Component
import play.api.libs.json.{JodaReads, Json}
import play.api.libs.ws.DefaultBodyWritables._
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.{ExecutionContext, Future}

@Component
class WhakaokoClient @Autowired()(@Value("#{config['whakaoko.url']}") whakaokoUrl: String,
                                  @Value("#{config['whakaoko.username']}") whakaokoUsername: String,
                                  @Value("#{config['whakaoko.channel']}") whakaokoChannel: String,
                                  feedReaderTaskExecutor: TaskExecutor) extends JodaReads {

  private val log = Logger.getLogger(classOf[FeedReaderRunner])

  implicit val executionContext = ExecutionContext.fromExecutor(feedReaderTaskExecutor)
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  private val wsClient = StandaloneAhcWSClient()

  private implicit val llr = Json.reads[LatLong]
  private implicit val pr = Json.reads[Place]
  private implicit val fir = Json.reads[FeedItem]
  private implicit val sr = Json.reads[Subscription]

  def createFeedSubscription(feedUrl: String): Future[Option[Subscription]] = {
    val createFeedSubscriptionUrl =  whakaokoUrl + "/" + whakaokoUsername + "/" + "/subscriptions/feeds"
    log.info("Posting new feed to: " + createFeedSubscriptionUrl)
    val params: Map[String, Seq[String]] = Map {
      "channel" -> Seq(whakaokoChannel)
      "url" -> Seq(feedUrl)
    }

    wsClient.url(createFeedSubscriptionUrl).post(params).map { r =>
      log.info("New feed result: " + r.status + " / " + r.body)
      if (r.status == 200) {
        Some(Json.parse(r.body).as[Subscription])
      } else {
        None
      }
    }
  }

  def getChannelFeedItems(page: Int = 1): Future[Seq[FeedItem]] = {
    val channelItemsUrl = whakaokoUrl + "/" + whakaokoUsername + "/channels/" + whakaokoChannel + "/items?page=" + page
    wsClient.url(channelItemsUrl).get.map { r =>
      if (r.status == 200) {
        Json.parse(r.body).as[Seq[FeedItem]]
      } else {
        Seq.empty
      }
    }
  }

  def getChannelSubscriptions(): Future[Seq[Subscription]] = {
    val channelSubscriptionsUrl = whakaokoUrl + "/" + whakaokoUsername + "/channels/" + whakaokoChannel + "/subscriptions"
    log.info("Fetching from: " + channelSubscriptionsUrl)
    wsClient.url(channelSubscriptionsUrl).get.map { r =>
      if (r.status == 200) {
        Json.parse(r.body).as[Seq[Subscription]]
      } else {
        log.warn("Get channel subscriptions failed (" + channelSubscriptionsUrl + "): " + r.status + " / " + r.body)
        Seq.empty
      }
    }
  }

  def getSubscriptionFeedItems(subscriptionId: String): Future[Seq[FeedItem]] = {
    val subscriptionItemsUrl = whakaokoUrl + "/" + whakaokoUsername + "/subscriptions/" + subscriptionId + "/items"
    wsClient.url(subscriptionItemsUrl).get.map { r =>
      if (r.status == 200) {
        Json.parse(r.body).as[Seq[FeedItem]]
      } else {
        Seq.empty
      }
    }
  }

}
