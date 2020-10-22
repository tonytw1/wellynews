package nz.co.searchwellington.feeds.whakaoko

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.model.{FeedItem, LatLong, Place, Subscription}
import org.apache.http.HttpStatus
import org.apache.log4j.Logger
import org.joda.time.{DateTime, Duration}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import play.api.libs.json.Json
import play.api.libs.json.Reads.DefaultJodaDateReads
import play.api.libs.ws.DefaultBodyWritables._
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.libs.ws.StandaloneWSRequest
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.{ExecutionContext, Future}

@Component
class WhakaokoClient @Autowired()(@Value("${whakaoko.url}") whakaokoUrl: String,
                                  @Value("${whakaoko.username}") whakaokoUsername: String,
                                  @Value("${whakaoko.channel}") whakaokoChannel: String,
                                  @Value("${whakaoko.token}") whakaokoToken: String) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[WhakaokoClient])

  private val ApplicationJsonHeader = "Content-Type" -> "application/json; charset=UTF8"

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  private val wsClient = StandaloneAhcWSClient()

  private implicit val dr = DefaultJodaDateReads
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

    withWhakaokoAuth(wsClient.url(createFeedSubscriptionUrl)).
      withRequestTimeout(TenSeconds).
      post(params).map { r =>
      log.debug("New feed result: " + r.status + " / " + r.body)
      if (r.status == HttpStatus.SC_OK) {
        Some(Json.parse(r.body).as[Subscription])
      } else {
        None
      }
    }
  }

  def getSubscription(subscriptionId: String)(implicit ec: ExecutionContext): Future[Option[Subscription]] = {
    withWhakaokoAuth(wsClient.url(subscriptionUrl(subscriptionId))).
      withRequestTimeout(TenSeconds).
      get.map { r =>
      if (r.status == HttpStatus.SC_OK) {
        Some(Json.parse(r.body).as[Subscription])
      } else {
        None
      }
    }
  }

  def getChannelFeedItems(page: Int)(implicit ec: ExecutionContext): Future[Seq[FeedItem]] = {
    log.info("Fetching channel items page: " + page)
    val channelItemsUrl = whakaokoUrl + "/" + "/channels/" + whakaokoChannel + "/items"
    val start = DateTime.now()

    withWhakaokoAuth(wsClient.url(channelItemsUrl)).
      addQueryStringParameters("page" -> page.toString).
      withRequestTimeout(TenSeconds).
      get.map { r =>
      log.info("Channel channel items returned after: " + new Duration(start, DateTime.now).getMillis)
      if (r.status == HttpStatus.SC_OK) {
        Json.parse(r.body).as[Seq[FeedItem]]
      } else {
        Seq.empty
      }
    }
  }

  def getChannelSubscriptions()(implicit ec: ExecutionContext): Future[Seq[Subscription]] = {
    val channelSubscriptionsUrl = whakaokoUrl + "/" + "/channels/" + whakaokoChannel + "/subscriptions"
    log.info("Fetching channel subscriptions from: " + (channelSubscriptionsUrl))
    val start = DateTime.now()

    withWhakaokoAuth(wsClient.url(channelSubscriptionsUrl)).
      withRequestTimeout(TenSeconds).
      get.map { r =>
      log.info("Channel subscriptions returned after: " + new Duration(start, DateTime.now).getMillis)
      if (r.status == HttpStatus.SC_OK) {
        Json.parse(r.body).as[Seq[Subscription]]
      } else {
        log.warn("Get channel subscriptions failed (" + (channelSubscriptionsUrl) + "): " + r.status + " / " + r.body)
        Seq.empty
      }
    }
  }

  def getSubscriptionFeedItems(subscriptionId: String)(implicit ec: ExecutionContext): Future[(Seq[FeedItem], Long)] = {
    withWhakaokoAuth(wsClient.url(subscriptionUrl(subscriptionId) + "/items")).
      withRequestTimeout(TenSeconds).
      get.map { r =>
      if (r.status == HttpStatus.SC_OK) {
        val feedItems: Seq[FeedItem] = Json.parse(r.body).as[Seq[FeedItem]]
        val totalCount: Long = r.header("x-total-count").map(c => c.toLong).getOrElse(feedItems.size)
        (feedItems, totalCount)
      } else {
        (Seq.empty, 0L)
      }
    }
  }

  def updateSubscriptionName(subscriptionId: String, title: String)(implicit ec: ExecutionContext): Future[Unit] = {
    implicit val supw = Json.writes[SubscriptionUpdateRequest]
    withWhakaokoAuth(wsClient.url(subscriptionUrl(subscriptionId))).
      withHttpHeaders(ApplicationJsonHeader).
      withRequestTimeout(TenSeconds).
      put(Json.toJson(SubscriptionUpdateRequest(name = title))).map { r =>
      log.debug("Update subscription name result: " + r.status + "/" + r.body)
      Unit
    }
  }

  private def subscriptionUrl(subscriptionId: String) = whakaokoUrl + "/subscriptions/" + subscriptionId

  private def withWhakaokoAuth(request: StandaloneWSRequest): StandaloneWSRequest = {
    request.addHttpHeaders("Authorization" -> ("Bearer " + whakaokoToken))
  }

  case class SubscriptionUpdateRequest(name: String)

}
