package nz.co.searchwellington.feeds.whakaoko

import nz.co.searchwellington.feeds.whakaoko.model.{FeedItem, Subscription}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class WhakaokoService @Autowired()(client: WhakaokoClient) {

  /*
   Whakaoko (https://github.com/tonytw1/whakaoko) is an RSS reading and aggregation service.
   It allows us to subscribe to an RSS feed URL and get the feed items back in JSON format.
   Multiple RSS feed subscriptions can be aggregated into a channel.
   This package handles the interactions with Whakaoko.
   This class seems to exist solely to wrap the client calls with Eithers
  */

  private val log = Logger.getLogger(classOf[WhakaokoService])

  def createFeedSubscription(feedUrl: String)(implicit ec: ExecutionContext): Future[Option[String]] = {
    log.info("Requesting Whakaoko subscription for feed: " + feedUrl)
    client.createFeedSubscription(feedUrl).map { cso =>
      cso.map(_.id)
    }
  }

  def getSubscriptions()(implicit ec: ExecutionContext): Future[Seq[Subscription]] = client.getChannelSubscriptions() // TODO catch errors

  def getSubscription(subscriptionID: String)(implicit ec: ExecutionContext): Future[Option[Subscription]] = { // TODO catch errors
    client.getSubscription(subscriptionID)
  }

  def getSubscriptionFeedItems(subscriptionId: String)(implicit ec: ExecutionContext): Future[Either[String, (Seq[FeedItem], Long)]] = {
    client.getSubscriptionFeedItems(subscriptionId).map { r =>
      Right(r)
    }.recover {
      case e: Throwable => Left(s"Failed to fetch feed items: ${e.getMessage}")
    }
  }

  def getChannelFeedItems(page: Int, subscriptions: Option[Seq[String]])(implicit ec: ExecutionContext): Future[Either[String, Seq[FeedItem]]] = {
    client.getChannelFeedItems(page, subscriptions).map { r =>
      Right(r)
    }.recover{
      case e: Throwable =>Left(s"Failed to fetch channel feed items: ${e.getLocalizedMessage}")
    }
  }

  def updateSubscriptionName(subscriptionId: String, title: String)(implicit ec: ExecutionContext): Future[Unit] = {
      client.updateSubscriptionName(subscriptionId, title)
  }

}
