package nz.co.searchwellington.feeds.reading

import nz.co.searchwellington.feeds.reading.whakaoko.WhakaokoClient
import nz.co.searchwellington.feeds.reading.whakaoko.model.{FeedItem, Subscription}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class WhakaokoService @Autowired()(client: WhakaokoClient) {

  private val log = Logger.getLogger(classOf[WhakaokoService])

  def createFeedSubscription(feedUrl: String): Future[Option[String]] = {
    log.info("Requesting Whakaoko subscription for feed: " + feedUrl)
    client.createFeedSubscription(feedUrl).map { cso =>
      cso.map(_.id)
    }
  }

  def getSubscriptions(): Future[Seq[Subscription]] = client.getChannelSubscriptions()


  def getWhakaokoSubscriptionByUrl(url: String): Future[Option[Subscription]] = {
    client.getChannelSubscriptions.map { channelSubscriptions =>
      // TODO API should allow us to pass the url rather than scanning the entire collection
    channelSubscriptions.find(s => s.url == url)
    }
  }

  def getSubscriptionFeedItems(subscriptionId: String): Future[Either[String, Seq[FeedItem]]] = {
      client.getSubscriptionFeedItems(subscriptionId).map { r =>
        Right(r)
      }
  }

  def getChannelFeedItems(page: Int): Future[Seq[FeedItem]] = client.getChannelFeedItems(page)

}
