package nz.co.searchwellington.feeds.whakaoko

import nz.co.searchwellington.feeds.whakaoko.model.{FeedItem, Subscription}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class WhakaokoService @Autowired()(client: WhakaokoClient) {

  private val log = Logger.getLogger(classOf[WhakaokoService])

  def createFeedSubscription(feedUrl: String)(implicit ec: ExecutionContext): Future[Option[String]] = {
    log.info("Requesting Whakaoko subscription for feed: " + feedUrl)
    client.createFeedSubscription(feedUrl).map { cso =>
      cso.map(_.id)
    }
  }

  def getSubscriptions()(implicit ec: ExecutionContext): Future[Seq[Subscription]] = client.getChannelSubscriptions()

  def getWhakaokoSubscriptionByUrl(url: String)(implicit ec: ExecutionContext): Future[Option[Subscription]] = {
    client.getChannelSubscriptions(url = Some(url)).map { subscriptionsByUrl =>
      subscriptionsByUrl.headOption
    }
  }

  def getSubscriptionFeedItems(subscriptionId: String)(implicit ec: ExecutionContext): Future[Either[String, Seq[FeedItem]]] = {
    client.getSubscriptionFeedItems(subscriptionId).map { r =>
      Right(r)
    }
  }

  def getChannelFeedItems(page: Int)(implicit ec: ExecutionContext): Future[Seq[FeedItem]] = client.getChannelFeedItems(page)

}