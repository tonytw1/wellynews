package nz.co.searchwellington.feeds.whakaoko

import nz.co.searchwellington.feeds.whakaoko.model.{FeedItem, Subscription}
import nz.co.searchwellington.model.Feed
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

  def getSubscriptions()(implicit ec: ExecutionContext): Future[Seq[Subscription]] = client.getChannelSubscriptions() // TODO catch errors

  def getWhakaokoSubscriptionFor(feed: Feed)(implicit ec: ExecutionContext): Future[Option[Subscription]] = { // TODO catch errors
    feed.whakaokoSubscription.map { sid =>
      client.getSubscription(sid)
    }.getOrElse{
      Future.successful(None)
    }
  }

  def getSubscriptionFeedItems(subscriptionId: String)(implicit ec: ExecutionContext): Future[Either[String, (Seq[FeedItem], Long)]] = {
    client.getSubscriptionFeedItems(subscriptionId).map { r =>
      Right(r)
    }.recover {
      case e: Throwable => Left(s"Failed to fetch feed items: ${e.getMessage}")
    }
  }

  def getChannelFeedItems(page: Int)(implicit ec: ExecutionContext): Future[Seq[FeedItem]] = client.getChannelFeedItems(page)

  def updateSubscriptionName(subscriptionId: String, title: String)(implicit ec: ExecutionContext): Future[Unit] = {
      client.updateSubscriptionName(subscriptionId, title)
  }

}
