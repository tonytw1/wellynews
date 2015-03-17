package nz.co.searchwellington.repositories

import nz.co.searchwellington.feeds.FeedItemLocalCopyDecorator
import nz.co.searchwellington.feeds.reading.{WhakaokoFeedItemMapper, WhakaoroService}
import nz.co.searchwellington.model.frontend.{FeedNewsitemForAcceptance, FrontendFeedNewsitem, FrontendNewsitem}
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._

@Component class SuggestedFeeditemsService @Autowired() (whakaoroService: WhakaoroService,
                                                        whakaokoFeedItemMapper: WhakaokoFeedItemMapper, feedItemLocalCopyDecorator: FeedItemLocalCopyDecorator,
                                                        resourceDAO: HibernateResourceDAO) {

  private val log: Logger = Logger.getLogger(classOf[SuggestedFeeditemsService])

  def getSuggestionFeednewsitems(maxItems: Int): List[FrontendNewsitem] = {
      val channelFeedItems: List[uk.co.eelpieconsulting.whakaoro.client.model.FeedItem] = whakaoroService.getChannelFeedItems.toList
      val notIgnoredFeedItems: List[FrontendFeedNewsitem] = channelFeedItems.map(i => fromWhakaoro(i)).filter(i => isNotIgnored(i))

      val suggestions: List[FeedNewsitemForAcceptance] = feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(notIgnoredFeedItems).toList
      val withLocalCopiesFilteredOut: List[FeedNewsitemForAcceptance] = suggestions.filter(i => noLocalCopy(i))

    withLocalCopiesFilteredOut.map(i => i.getFeednewsitem)  // TODO suspect we then redecorate these again?
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