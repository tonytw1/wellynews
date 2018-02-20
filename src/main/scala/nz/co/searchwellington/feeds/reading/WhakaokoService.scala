package nz.co.searchwellington.feeds.reading

import java.io.UnsupportedEncodingException

import com.google.common.collect.Lists
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.http.{HttpBadRequestException, HttpFetchException, HttpForbiddenException, HttpNotFoundException}
import uk.co.eelpieconsulting.whakaoro.client.WhakaoroClient
import uk.co.eelpieconsulting.whakaoro.client.exceptions.ParsingException
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Component class WhakaokoService @Autowired()(@Value("#{config['whakaoko.url']}") whakaokoUrl: String,
                                              @Value("#{config['whakaoko.username']}") whakaokoUsername: String,
                                              @Value("#{config['whakaoko.channel']}") whakaokoChannel: String) {

  private val log = Logger.getLogger(classOf[WhakaokoService])

  def createFeedSubscription(feedUrl: String): String = {
    log.info("Requesting Whakakaoro subscription for feed")
    try {
      val createdFeedSubscription = getClient.createFeedSubscription(whakaokoUsername, whakaokoChannel, feedUrl)
      createdFeedSubscription.getId
    }
    catch {
      case e: UnsupportedEncodingException => {
        log.error(e)
        null
      }
      case e: HttpNotFoundException => {
        log.error(e)
        null
      }
      case e: HttpBadRequestException => {
        log.error(e)
        null
      }
      case e: HttpForbiddenException => {
        log.error(e)
        null
      }
      case e: HttpFetchException => {
        log.error(e)
        null
      }
      case e: ParsingException => {
        log.error(e)
        null
      }
    }
  }

  def getSubscriptionFeedItems(subscriptionId: String): Seq[FeedItem] = {
    import scala.collection.JavaConversions._
    try {
       getClient.getSubscriptionFeedItems(whakaokoUsername, subscriptionId)
    }
    catch {
      case e: HttpNotFoundException => {
        log.error(e)
        Seq()
      }
      case e: HttpBadRequestException => {
        log.error(e)
        Seq()
      }
      case e: HttpForbiddenException => {
        log.error(e)
        Seq()
      }
      case e: HttpFetchException => {
        log.error(e)
        Seq()
      }
      case e: ParsingException => {
        log.error(e)
        Seq()
      }
    }
  }

  def getChannelFeedItems(): java.util.List[FeedItem] = {
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
      getClient.getChannelFeedItems(whakaokoUsername, whakaokoChannel, 0) // TODO restore pagination

    } catch {
      case e: HttpNotFoundException => {
        log.error(e)
        return Lists.newArrayList()
      }
      case e: HttpBadRequestException => {
        log.error(e)
        return Lists.newArrayList()
      }
      case e: HttpForbiddenException => {
        log.error(e)
        return Lists.newArrayList()
      }
      case e: HttpFetchException => {
        log.error(e)
        return Lists.newArrayList()
      }
      case e: ParsingException => {
        log.error(e)
        return Lists.newArrayList()
      }
    }
  }

  private def getClient: WhakaoroClient = {
    log.info("Creating whakaoko client for: " + whakaokoUrl)
    new WhakaoroClient(whakaokoUrl)
  }

}
