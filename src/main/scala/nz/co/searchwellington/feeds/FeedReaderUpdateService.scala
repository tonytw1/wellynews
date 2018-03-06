package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.{Feed, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.tagging.AutoTaggingService
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Component class FeedReaderUpdateService(contentUpdateService: ContentUpdateService, autoTagger: AutoTaggingService, feednewsItemToNewsitemService: FeeditemToNewsitemService) {

  def acceptNewsitem(feedReaderUser: User, feednewsitem: FeedItem, feed: Feed): Int = {
    val newsitem = feednewsItemToNewsitemService.makeNewsitemFromFeedItem(feednewsitem, Some(feed))
    contentUpdateService.create(newsitem)
    autoTagger.autotag(newsitem)
    contentUpdateService.update(newsitem)
    newsitem.id
  }

}
