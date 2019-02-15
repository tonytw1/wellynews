package nz.co.searchwellington.feeds.reading

import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.WhakaoroClient
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Component class WhakaokoService @Autowired()(@Value("#{config['whakaoko.url']}") whakaokoUrl: String,
                                              @Value("#{config['whakaoko.username']}") whakaokoUsername: String,
                                              @Value("#{config['whakaoko.channel']}") whakaokoChannel: String) {

  private val log = Logger.getLogger(classOf[WhakaokoService])

  private val client: WhakaoroClient = new WhakaoroClient(whakaokoUrl)

  def createFeedSubscription(feedUrl: String): Option[String] = {
    log.info("Requesting Whakaoko subscription for feed: " + feedUrl)
    try {
      val createdFeedSubscription = client.createFeedSubscription(whakaokoUsername, whakaokoChannel, feedUrl)
      Some(createdFeedSubscription.getId)
    }
    catch {
      case e: Exception => {
        log.error(e)
        None
      }
    }
  }

  def getSubscriptionFeedItems(subscriptionId: String): Seq[FeedItem] = {
    try {
      import scala.collection.JavaConversions._
      client.getSubscriptionFeedItems(whakaokoUsername, subscriptionId)
    }
    catch {
      case e: Exception => {
        log.error(e)
        Seq()
      }
    }
  }

  def getChannelFeedItems(): Seq[FeedItem] = {
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

      import scala.collection.JavaConversions._
      client.getChannelFeedItems(whakaokoUsername, whakaokoChannel, 0) // TODO restore pagination

    } catch {
      case e: Exception => {
        log.error(e)
        Seq()
      }
    }
  }

}
