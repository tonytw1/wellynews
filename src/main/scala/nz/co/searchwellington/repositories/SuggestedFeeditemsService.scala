package nz.co.searchwellington.repositories

import java.util.List
import nz.co.searchwellington.feeds.FeedItemLocalCopyDecorator
import nz.co.searchwellington.feeds.reading.WhakaokoFeedItemMapper
import nz.co.searchwellington.feeds.reading.WhakaoroClientFactory
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.model.FeedAcceptancePolicy
import nz.co.searchwellington.model.frontend.FeedNewsitemForAcceptance
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem
import nz.co.searchwellington.model.frontend.FrontendNewsitem
import org.apache.log4j.Logger
import org.elasticsearch.common.collect.Lists
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.http.HttpBadRequestException
import uk.co.eelpieconsulting.common.http.HttpFetchException
import uk.co.eelpieconsulting.common.http.HttpForbiddenException
import uk.co.eelpieconsulting.common.http.HttpNotFoundException
import uk.co.eelpieconsulting.whakaoro.client.exceptions.ParsingException
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Component object SuggestedFeeditemsService {
  private val log: Logger = Logger.getLogger(classOf[SuggestedFeeditemsService])
}

@Component class SuggestedFeeditemsService {
  private final val feedItemLocalCopyDecorator: FeedItemLocalCopyDecorator = null
  private final val whakaoroClientFactory: WhakaoroClientFactory = null
  private final val whakaokoFeedItemMapper: WhakaokoFeedItemMapper = null
  private final val resourceDAO: HibernateResourceDAO = null

  @Autowired def this(whakaoroClientFactory: WhakaoroClientFactory, whakaokoFeedItemMapper: WhakaokoFeedItemMapper, feedItemLocalCopyDecorator: FeedItemLocalCopyDecorator, resourceDAO: HibernateResourceDAO) {
    this()
    this.whakaoroClientFactory = whakaoroClientFactory
    this.whakaokoFeedItemMapper = whakaokoFeedItemMapper
    this.feedItemLocalCopyDecorator = feedItemLocalCopyDecorator
    this.resourceDAO = resourceDAO
  }

  def getSuggestionFeednewsitems(maxItems: Int): List[FrontendNewsitem] = {
    try {
      val channelFeedItemsForNotIgnoredFeeds: List[FrontendFeedNewsitem] = Lists.newArrayList
      import scala.collection.JavaConversions._
      for (feedItem <- whakaoroClientFactory.getChannelFeedItems) {
        val feed: Feed = resourceDAO.loadFeedByWhakaoroId(feedItem.getSubscriptionId)
        if (feed == null) {
          SuggestedFeeditemsService.log.info("Ignoring feed item with unknown whakaoro id: " + feedItem.getSubscriptionId)
          continue //todo: continue is not supported
        }
        if (feed.getAcceptancePolicy == FeedAcceptancePolicy.IGNORE) {
          continue //todo: continue is not supported
        }
        channelFeedItemsForNotIgnoredFeeds.add(whakaokoFeedItemMapper.mapWhakaokoFeeditem(feed, feedItem))
      }
      val addSupressionAndLocalCopyInformation: List[FeedNewsitemForAcceptance] = feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(channelFeedItemsForNotIgnoredFeeds)
      val suggestions: List[FrontendNewsitem] = Lists.newArrayList
      import scala.collection.JavaConversions._
      for (feedNewsitemForAcceptance <- addSupressionAndLocalCopyInformation) {
        if (feedNewsitemForAcceptance.getAcceptanceState.getLocalCopy != null) {
          continue //todo: continue is not supported
        }
        suggestions.add(feedNewsitemForAcceptance.getFeednewsitem)
      }
      return suggestions
    }
    catch {
      case e: HttpNotFoundException => {
        SuggestedFeeditemsService.log.error(e)
      }
      case e: HttpBadRequestException => {
        SuggestedFeeditemsService.log.error(e)
      }
      case e: HttpForbiddenException => {
        SuggestedFeeditemsService.log.error(e)
      }
      case e: ParsingException => {
        SuggestedFeeditemsService.log.error(e)
      }
      case e: HttpFetchException => {
        SuggestedFeeditemsService.log.error(e)
      }
    }
    return Lists.newArrayList
  }
}