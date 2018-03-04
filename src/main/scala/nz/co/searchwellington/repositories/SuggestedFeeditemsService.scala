package nz.co.searchwellington.repositories

import nz.co.searchwellington.feeds.FeedItemLocalCopyDecorator
import nz.co.searchwellington.feeds.reading.{WhakaokoFeedItemMapper, WhakaokoService}
import nz.co.searchwellington.model.frontend.{FeedNewsitemForAcceptance, FrontendNewsitem}
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Component class SuggestedFeeditemsService @Autowired() (whakaoroService: WhakaokoService,
                                                         whakaokoFeedItemMapper: WhakaokoFeedItemMapper,
                                                         feedItemLocalCopyDecorator: FeedItemLocalCopyDecorator,
                                                         resourceDAO: HibernateResourceDAO) {

  private val log = Logger.getLogger(classOf[SuggestedFeeditemsService])

  def getSuggestionFeednewsitems(maxItems: Int): Seq[FeedItem] = {

    val channelFeedItems = whakaoroService.getChannelFeedItems
    val notIgnoredFeedItems = channelFeedItems.map(i => fromWhakaoko(i)).filter(i => isNotIgnored(i))
    val suggestions = feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(notIgnoredFeedItems)
    val withLocalCopiesFilteredOut = suggestions.filter(i => noLocalCopy(i))

    withLocalCopiesFilteredOut.map(i => i.getFeednewsitem)
  }

  private def fromWhakaoko(feedItem: uk.co.eelpieconsulting.whakaoro.client.model.FeedItem): FeedItem = {
    val feed = resourceDAO.loadFeedByWhakaoroId(feedItem.getSubscriptionId)
    whakaokoFeedItemMapper.mapWhakaokoFeeditem(feed, feedItem)
  }

  private def isNotIgnored(feedItem: FeedItem) : Boolean = {
    // feedItem.getFeed != null && feedItem.getFeed.getAcceptancePolicy != FeedAcceptancePolicy.IGNORE
    true  // TODO feed needs to be available
  }

  private def noLocalCopy(feedItem: FeedNewsitemForAcceptance) : Boolean = {
    feedItem.getAcceptanceState.getLocalCopy == null
  }

}
