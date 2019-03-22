package nz.co.searchwellington.repositories

import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, FeeditemToNewsitemService, RssfeedNewsitemService}
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.model.frontend.{FeedNewsitemForAcceptance, FrontendResource}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Component class SuggestedFeeditemsService @Autowired() (rssfeedNewsitemService: RssfeedNewsitemService,
                                                         feedItemLocalCopyDecorator: FeedItemLocalCopyDecorator,
                                                         resourceDAO: HibernateResourceDAO,
                                                         feeditemToNewsitemService: FeeditemToNewsitemService) {

  private val log = Logger.getLogger(classOf[SuggestedFeeditemsService])

  def getSuggestionFeednewsitems(maxItems: Int): Seq[FrontendResource] = {
    val channelFeedItems = rssfeedNewsitemService.getFeedItems()
    val notIgnoredFeedItems: Seq[(FeedItem, Option[Feed])] = channelFeedItems.filter(i => isNotIgnored(i._1))

    val channelNewsitems = notIgnoredFeedItems.map(i => feeditemToNewsitemService.makeNewsitemFromFeedItem(i._1, i._2.get)) // TODO Naked get

    val suggestions = feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(channelNewsitems)
    val withLocalCopiesFilteredOut = suggestions.filter(i => noLocalCopy(i))

    withLocalCopiesFilteredOut.map(i => i.getFeednewsitem)
  }

  private def isNotIgnored(feedItem: FeedItem) : Boolean = {
    // feedItem.getFeed != null && feedItem.getFeed.getAcceptancePolicy != FeedAcceptancePolicy.IGNORE
    true  // TODO feed needs to be available
  }

  private def noLocalCopy(feedItem: FeedNewsitemForAcceptance) : Boolean = {
    feedItem.getAcceptanceState.getLocalCopy == null  // TODO Really?
  }

}
