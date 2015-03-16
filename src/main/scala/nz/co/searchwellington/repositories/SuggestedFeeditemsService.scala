package nz.co.searchwellington.repositories

import nz.co.searchwellington.feeds.FeedItemLocalCopyDecorator
import nz.co.searchwellington.feeds.reading.{WhakaokoFeedItemMapper, WhakaoroClientFactory}
import nz.co.searchwellington.model.frontend.{FeedNewsitemForAcceptance, FrontendFeed, FrontendFeedNewsitem, FrontendNewsitem}
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.http.{HttpBadRequestException, HttpFetchException, HttpForbiddenException, HttpNotFoundException}
import uk.co.eelpieconsulting.whakaoro.client.exceptions.ParsingException

import scala.collection.JavaConversions._

@Component class SuggestedFeeditemsService @Autowired()(whakaoroClientFactory: WhakaoroClientFactory,
                                                        whakaokoFeedItemMapper: WhakaokoFeedItemMapper, feedItemLocalCopyDecorator: FeedItemLocalCopyDecorator,
                                                        resourceDAO: HibernateResourceDAO) {

  private val log: Logger = Logger.getLogger(classOf[SuggestedFeeditemsService])

  def getSuggestionFeednewsitems(maxItems: Int): List[FrontendNewsitem] = {
    try {
      val channelFeedItems: List[uk.co.eelpieconsulting.whakaoro.client.model.FeedItem] = whakaoroClientFactory.getChannelFeedItems.toList
      val notIgnoredFeedItems: List[FrontendFeedNewsitem] = channelFeedItems.map(i => fromWhakaoro(i)).filter(i => isNotIgnored(i))
      val suggestions: List[FeedNewsitemForAcceptance] = feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(notIgnoredFeedItems).toList
      return suggestions.filter(i => noLocalCopy(i))
    }
    catch {
      case e: HttpNotFoundException => {
        log.error(e)
      }
      case e: HttpBadRequestException => {
        log.error(e)
      }
      case e: HttpForbiddenException => {
        log.error(e)
      }
      case e: ParsingException => {
        log.error(e)
      }
      case e: HttpFetchException => {
        log.error(e)
      }
    }
    List.empty
  }

  def fromWhakaoro(feedItem: uk.co.eelpieconsulting.whakaoro.client.model.FeedItem): FrontendFeedNewsitem = {
    val feed: Feed = resourceDAO.loadFeedByWhakaoroId(feedItem.getSubscriptionId)
    whakaokoFeedItemMapper.mapWhakaokoFeeditem(feed, feedItem)
  }

  def isNotIgnored(feedItem: FrontendFeedNewsitem) : Boolean = {
    feedItem.getFeed != null && feedItem.getFeed.getAcceptancePolicy != FeedAcceptancePolicy.IGNORE
  }

  def noLocalCopy(feedItem: FeedNewsitemForAcceptance) : Boolean = {
    feedItem.getAcceptanceState.getLocalCopy == null
  }

}