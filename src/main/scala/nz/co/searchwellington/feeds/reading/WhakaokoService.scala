package nz.co.searchwellington.feeds.reading

import java.io.UnsupportedEncodingException

import org.apache.log4j.Logger
import org.elasticsearch.common.collect.Lists
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.http.{HttpBadRequestException, HttpFetchException, HttpForbiddenException, HttpNotFoundException}
import uk.co.eelpieconsulting.whakaoro.client.WhakaoroClient
import uk.co.eelpieconsulting.whakaoro.client.exceptions.ParsingException
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Component class WhakaokoService @Autowired()(val url: String, val username: String, val channel: String) {

  private val log = Logger.getLogger(classOf[WhakaokoService])

  def createFeedSubscription(url: String): String = {
    log.info("Requesting Whakakaoro subscription for feed")
    try {
      val createdFeedSubscription = getClient.createFeedSubscription(username, channel, url)
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
       getClient.getSubscriptionFeedItems(username, subscriptionId)
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
      getClient.getChannelFeedItems(username, channel, 0) // TODO restore pagination
    }
    catch {
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
    return new WhakaoroClient(url)
  }

}