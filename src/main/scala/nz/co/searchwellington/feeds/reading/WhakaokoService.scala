package nz.co.searchwellington.feeds.reading

import nz.co.searchwellington.feeds.reading.whakaoko.model.Subscription
import nz.co.searchwellington.feeds.reading.whakaoko.{WhakaokoClient, model}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Component

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Component class WhakaokoService @Autowired()(@Value("#{config['whakaoko.url']}") whakaokoUrl: String,
                                              @Value("#{config['whakaoko.username']}") whakaokoUsername: String,
                                              @Value("#{config['whakaoko.channel']}") whakaokoChannel: String,
                                              feedReaderTaskExecutor: TaskExecutor) {

  private val log = Logger.getLogger(classOf[WhakaokoService])

  private val client = new WhakaokoClient(whakaokoUrl, feedReaderTaskExecutor)

  def createFeedSubscription(feedUrl: String): Future[Option[String]] = {
    log.info("Requesting Whakaoko subscription for feed: " + feedUrl)
    client.createFeedSubscription(whakaokoUsername, whakaokoChannel, feedUrl).map { cso =>
      cso.map(_.id)
    }
  }

  def getSubscriptions(): Future[Seq[Subscription]] = {
    client.getChannelSubscriptions(whakaokoUsername, whakaokoChannel)
  }

  def getWhakaokoSubscriptionByUrl(url: String): Future[Option[Subscription]] = {
    client.getChannelSubscriptions(whakaokoUsername, whakaokoChannel).map { channelSubscriptions =>
      // TODO API should allow us to pass the url rather than scanning the entire collection
    channelSubscriptions.find(s => s.url == url)
    }
  }

  def getSubscriptionFeedItems(subscriptionId: String): Future[Either[String, Seq[model.FeedItem]]] = {
      client.getSubscriptionFeedItems(whakaokoUsername, subscriptionId).map { r =>
        Right(r)
      }
  }

  def getChannelFeedItems(): Future[Seq[model.FeedItem]] = {
    try {
      /*
      val channelFeedItems: java.util.List[FeedItem] = Lists.newArrayList()
      var page: Int = 0
      while (page <= 5) {
        {
        }
        ({
          page += 1;
          page - 1
        })
      }
      */

      client.getChannelFeedItems(whakaokoUsername, whakaokoChannel, 0) // TODO restore pagination

    } catch {
      case e: Exception => {
        log.error(e)
        Future.successful(Seq())
      }
    }
  }

}
