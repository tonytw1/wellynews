package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.{Feed, Newsitem, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.tagging.AutoTaggingService
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class FeedReaderUpdateService(contentUpdateService: ContentUpdateService, autoTagger: AutoTaggingService, feednewsItemToNewsitemService: FeeditemToNewsitemService) {

  def acceptNewsitem(feedReaderUser: User, feednewsitem: FeedItem, feed: Feed): Future[Newsitem] = {
    val newsitem = feednewsItemToNewsitemService.makeNewsitemFromFeedItem(feednewsitem, feed)
    contentUpdateService.create(newsitem).map { _ =>
      autoTagger.autotag(newsitem)
      contentUpdateService.update(newsitem)
      newsitem
    }
  }

}
