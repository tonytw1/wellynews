package nz.co.searchwellington.feeds.reading.whakaoko

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import nz.co.searchwellington.feeds.FeedReaderRunner
import nz.co.searchwellington.feeds.reading.whakaoko.model.{FeedItem, Subscription}
import org.apache.log4j.Logger
import org.springframework.core.task.TaskExecutor
import play.api.libs.json.{JodaReads, Json}
import play.api.libs.ws.DefaultBodyWritables._
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.{ExecutionContext, Future}

class WhakaokoClient(whakaokoUrl: String, feedReaderTaskExecutor: TaskExecutor) extends JodaReads {

  private val log = Logger.getLogger(classOf[FeedReaderRunner])

  implicit val executionContext = ExecutionContext.fromExecutor(feedReaderTaskExecutor)
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  private val wsClient = StandaloneAhcWSClient()


  private implicit val fir = Json.reads[FeedItem]
  private implicit val sr = Json.reads[Subscription]

  def createFeedSubscription(whakaokoUsername: String, whakaokoChannel: String, feedUrl: String): Future[Option[Subscription]] = {
    val createFeedSubscriptionUrl = whakaokoUsername + "/" + whakaokoUsername + "/subscriptions/feeds"

    val params: Map[String, Seq[String]] = Map{
      "channel" -> Seq(whakaokoChannel)
      "url" -> Seq(feedUrl)
    }

    wsClient.url(createFeedSubscriptionUrl).post(params).map { r =>
      if (r.status == 200) {
        Some(Json.parse(r.body).as[Subscription])
      } else {
        None
      }
    }
  }

  def getChannelFeedItems(whakaokoUsername: String, whakaokoChannel: String, i: Int): Future[Seq[FeedItem]] = {
    val channelItemsUrl = whakaokoUrl + "/" + whakaokoUsername + "/channels/" + whakaokoChannel + "/items"
    wsClient.url(channelItemsUrl).get.map { r =>
      if (r.status == 200) {
        Json.parse(r.body).as[Seq[FeedItem]]
      } else {
        Seq.empty
      }
    }
  }

  def getChannelSubscriptions(whakaokoUsername: String, whakaokoChannelId: String): Future[Seq[Subscription]] = {
    val channelSubscriptionsUrl = whakaokoUrl + "/" + whakaokoUsername + "/channels/" + whakaokoChannelId + "/subscriptions"
    wsClient.url(channelSubscriptionsUrl).get.map { r =>
      if (r.status == 200) {
        Json.parse(r.body).as[Seq[Subscription]]
      } else {
        log.warn("Get channel subscriptions failed (" + channelSubscriptionsUrl + "): " + r.status + " / " + r.body)
        Seq.empty
      }
    }
  }

  def getSubscriptionFeedItems(whakaokoUsername: String, subscriptionId: String): Future[Seq[FeedItem]] = {
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
