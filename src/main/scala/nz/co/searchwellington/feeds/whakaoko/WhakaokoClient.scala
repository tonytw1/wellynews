package nz.co.searchwellington.feeds.whakaoko

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import nz.co.searchwellington.feeds.whakaoko.model.{FeedItem, LatLong, Place, Subscription}
import org.apache.http.HttpStatus
import org.apache.log4j.Logger
import org.joda.time.{DateTime, Duration}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import play.api.libs.json.{JodaReads, Json}
import play.api.libs.ws.DefaultBodyWritables._
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.{ExecutionContext, Future}

@Component
class WhakaokoClient @Autowired()(@Value("${whakaoko.url}") whakaokoUrl: String,
                                  @Value("${whakaoko.username}") whakaokoUsername: String,
                                  @Value("${whakaoko.channel}") whakaokoChannel: String) extends JodaReads {

  private val log = Logger.getLogger(classOf[WhakaokoClient])

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  private val wsClient = StandaloneAhcWSClient()

  private implicit val llr = Json.reads[LatLong]
  private implicit val pr = Json.reads[Place]
  private implicit val fir = Json.reads[FeedItem]
  private implicit val sr = Json.reads[Subscription]

  def createFeedSubscription(feedUrl: String)(implicit ec: ExecutionContext): Future[Option[Subscription]] = {
    val createFeedSubscriptionUrl = whakaokoUrl + "/" + whakaokoUsername + "/subscriptions/feeds"
    log.debug("Posting new feed to: " + createFeedSubscriptionUrl)

    val params: Map[String, Seq[String]] = Map(
      "channel" -> Seq(whakaokoChannel),
      "url" -> Seq(feedUrl)
    )

    wsClient.url(createFeedSubscriptionUrl).post(params).map { r =>
      log.debug("New feed result: " + r.status + " / " + r.body)
      if (r.status == HttpStatus.SC_OK) {
        Some(Json.parse(r.body).as[Subscription])
      } else {
        None
      }
    }
  }

  def getChannelFeedItems(page: Int)(implicit ec: ExecutionContext): Future[Seq[FeedItem]] = {
    log.info("Fetching channel items page: " + page)
    val channelItemsUrl = whakaokoUrl + "/" + whakaokoUsername + "/channels/" + whakaokoChannel + "/items"
    val start = DateTime.now()
    wsClient.url(channelItemsUrl).addQueryStringParameters("page" -> page.toString).get.map { r =>
      log.info("Channel channel items returned after: " + new Duration(start, DateTime.now).getMillis)
      if (r.status == HttpStatus.SC_OK) {
        Json.parse(r.body).as[Seq[FeedItem]]
      } else {
        Seq.empty
      }
    }
  }

  def getChannelSubscriptions(url: Option[String] = None)(implicit ec: ExecutionContext): Future[Seq[Subscription]] = {
    val channelSubscriptionsRequest = wsClient.url(whakaokoUrl + "/" + whakaokoUsername + "/channels/" + whakaokoChannel + "/subscriptions")
    val withUrl = url.map { u =>
      channelSubscriptionsRequest.addQueryStringParameters(("url", u))
    }.getOrElse(channelSubscriptionsRequest)

    log.info("Fetching channel subscriptions from: " + (whakaokoUrl + "/" + whakaokoUsername + "/channels/" + whakaokoChannel + "/subscriptions"))
    val start = DateTime.now()
    withUrl.get.map { r =>
      log.info("Channel subscriptions returned after: " + new Duration(start, DateTime.now).getMillis)
      if (r.status == HttpStatus.SC_OK) {
        Json.parse(r.body).as[Seq[Subscription]]
      } else {
        log.warn("Get channel subscriptions failed (" + (whakaokoUrl + "/" + whakaokoUsername + "/channels/" + whakaokoChannel + "/subscriptions") + "): " + r.status + " / " + r.body)
        Seq.empty
      }
    }
  }

  def getSubscriptionFeedItems(subscriptionId: String)(implicit ec: ExecutionContext): Future[Seq[FeedItem]] = {
    val subscriptionItemsUrl = whakaokoUrl + "/" + whakaokoUsername + "/subscriptions/" + subscriptionId + "/items"
    wsClient.url(subscriptionItemsUrl).get.map { r =>
      if (r.status == HttpStatus.SC_OK) {
        Json.parse(r.body).as[Seq[FeedItem]]
      } else {
        Seq.empty
      }
    }
  }

}