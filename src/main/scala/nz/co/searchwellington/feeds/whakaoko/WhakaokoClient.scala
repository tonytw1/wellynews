package nz.co.searchwellington.feeds.whakaoko

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.model._
import nz.co.searchwellington.http.WSClient
import nz.co.searchwellington.instrumentation.SpanFactory
import org.apache.commons.logging.LogFactory
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.libs.ws.StandaloneWSRequest

import scala.concurrent.{ExecutionContext, Future}

@Component
class WhakaokoClient @Autowired()(@Value("${whakaoko.url}") whakaokoUrl: String,
                                  @Value("${whakaoko.channel}") whakaokoChannel: String,
                                  @Value("${whakaoko.token}") whakaokoToken: String,
                                  wsClient: WSClient) extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[WhakaokoClient])

  private val ApplicationJsonHeader = "Content-Type" -> "application/json; charset=UTF8"

  private val pageSize = 30

  def createFeedSubscription(feedUrl: String)(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[Subscription]] = {
    val span = startSpan(currentSpan, "createFeedSubscription")

    val createFeedSubscriptionUrl = whakaokoUrl + "/subscriptions"
    log.debug("Posting new feed to: " + createFeedSubscriptionUrl)
    val createSubscriptionRequest = CreateSubscriptionRequest(
      url = feedUrl,
      channel = whakaokoChannel
    )

    implicit val csrw: OWrites[CreateSubscriptionRequest] = Json.writes[CreateSubscriptionRequest]
    withWhakaokoAuth(wsClient.wsClient.url(createFeedSubscriptionUrl)).
      withRequestTimeout(TenSeconds).
      post(Json.toJson(createSubscriptionRequest)).map { r =>
      span.setAttribute("http.response.status", r.status)
      span.end()
      r.status match {
        case HttpStatus.SC_OK =>
          Some(Json.parse(r.body).as[Subscription])
        case _ =>
          None
      }
    }
  }

  def getSubscription(subscriptionId: String)(implicit ec: ExecutionContext, currentSpan: Span): Future[Option[Subscription]] = {
    val span = startSpan(currentSpan, "getSubscription")

    withWhakaokoAuth(wsClient.wsClient.url(subscriptionUrl(subscriptionId))).
      withRequestTimeout(TenSeconds).
      get.map { r =>
      span.setAttribute("http.response.status", r.status)
      span.end()
      r.status match {
        case HttpStatus.SC_OK =>
          Some(Json.parse(r.body).as[Subscription])
        case _ =>
          None
      }
    }
  }

  def getChannelFeedItems(page: Int, subscriptions: Option[Seq[String]])(implicit ec: ExecutionContext, currentSpan: Span): Future[Seq[FeedItem]] = {
    val span = startSpan(currentSpan, "getChannelFeedItems")
    log.info("Fetching channel items page: " + page)

    val channelItemsUrl = whakaokoUrl + "/" + "/channels/" + whakaokoChannel + "/items"
    val request = withWhakaokoAuth(wsClient.wsClient.url(channelItemsUrl)).
      addQueryStringParameters("page" -> page.toString).
      addQueryStringParameters("subscriptions" -> subscriptions.getOrElse(Seq.empty).mkString(",")). // TODO This is an Option - decide
      withRequestTimeout(TenSeconds)
    request.get.map { r =>
      span.setAttribute("http.response.status", r.status)
      span.end()
      r.status match {
        case HttpStatus.SC_OK => Json.parse(r.body).as[Seq[FeedItem]]
        case _ => Seq.empty
      }
    }
  }

  def getChannelSubscriptions()(implicit ec: ExecutionContext, currentSpan: Span): Future[Seq[Subscription]] = {
    val span = startSpan(currentSpan, "getChannelSubscriptions")

    val channelSubscriptionsUrl = whakaokoUrl + "/" + "/channels/" + whakaokoChannel + "/subscriptions"
    log.info("Fetching channel subscriptions from: " + channelSubscriptionsUrl)
    withWhakaokoAuth(wsClient.wsClient.url(channelSubscriptionsUrl).withQueryStringParameters("pageSize" -> pageSize.toString)).
      withRequestTimeout(TenSeconds).
      get.map { r =>
      span.setAttribute("http.response.status", r.status)
      span.end()
      r.status match {
        case HttpStatus.SC_OK => Json.parse(r.body).as[Seq[Subscription]]
        case _ =>
          log.warn("Get channel subscriptions failed (" + channelSubscriptionsUrl + "): " + r.status + " / " + r.body)
          Seq.empty
      }
    }
  }

  // Given a subscription id, return the first page of feed items and a total items count
  def getSubscriptionFeedItems(subscriptionId: String)(implicit ec: ExecutionContext, currentSpan: Span): Future[(Seq[FeedItem], Long)] = {
    val span = startSpan(currentSpan, "getSubscriptionFeedItems")

    withWhakaokoAuth(wsClient.wsClient.url(subscriptionUrl(subscriptionId) + "/items").withQueryStringParameters("pageSize" -> pageSize.toString)).
      withRequestTimeout(TenSeconds).
      get.map { r =>
      span.setAttribute("http.response.status", r.status)
      span.end()
      r.status match {
        case HttpStatus.SC_OK =>
          val feedItems = Json.parse(r.body).as[Seq[FeedItem]]
          val totalCount = r.header("x-total-count").map(c => c.toLong).getOrElse(feedItems.size.toLong)
          (feedItems, totalCount)
        case _ =>
          (Seq.empty, 0L)
      }
    }
  }

  def updateSubscriptionName(subscriptionId: String, title: String)(implicit ec: ExecutionContext, currentSpan: Span): Future[Unit] = {
    val span = startSpan(currentSpan, "updateSubscriptionName")

    implicit val supw: OWrites[SubscriptionUpdateRequest] = Json.writes[SubscriptionUpdateRequest]
    val request = wsClient.wsClient.url(subscriptionUrl(subscriptionId)).
      withHttpHeaders(ApplicationJsonHeader).
      withRequestTimeout(TenSeconds)

    withWhakaokoAuth(request).put(Json.toJson(SubscriptionUpdateRequest(name = title))).map { r =>
      span.setAttribute("http.response.status", r.status)
      span.end()
      r.status match {
        case HttpStatus.SC_OK =>
          log.debug("Update subscription name result: " + r.status + "/" + r.body)
        case _ =>
          log.warn("Update subscription call failed: " + r.status + "/" + r.body)
      }
      ()
    }
  }

  private def subscriptionUrl(subscriptionId: String) = whakaokoUrl + "/subscriptions/" + subscriptionId

  private def withWhakaokoAuth(request: StandaloneWSRequest): StandaloneWSRequest = {
    request.addHttpHeaders("Authorization" -> ("Bearer " + whakaokoToken))
  }

  private case class CreateSubscriptionRequest(url: String, channel: String, name: Option[String] = None)

  private case class SubscriptionUpdateRequest(name: String)

  private def startSpan(currentSpan: Span, spanName: String): Span = {
    SpanFactory.childOf(currentSpan, spanName).
      setAttribute("database", "whakaoko").
      startSpan()
  }

}
