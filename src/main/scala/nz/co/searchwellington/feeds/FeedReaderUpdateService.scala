package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.{Feed, User}
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.tagging.AutoTaggingService
import org.springframework.stereotype.Component

@Component class FeedReaderUpdateService(contentUpdateService: ContentUpdateService, feedItemAcceptor: FeedItemAcceptor, autoTagger: AutoTaggingService, feednewsItemToNewsitemService: FeednewsItemToNewsitemService) {

  def acceptNewsitem(feed: Feed, feedReaderUser: User, feednewsitem: FrontendFeedNewsitem): Int = {
    val newsitem = feednewsItemToNewsitemService.makeNewsitemFromFeedItem(feed, feednewsitem)
    feedItemAcceptor.acceptFeedItem(feedReaderUser, newsitem)
    contentUpdateService.create(newsitem)
    autoTagger.autotag(newsitem)
    contentUpdateService.update(newsitem)
    newsitem.id
  }

}
