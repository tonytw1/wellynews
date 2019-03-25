package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.{Feed, Newsitem, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.tagging.AutoTaggingService
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class FeedReaderUpdateService(contentUpdateService: ContentUpdateService, autoTagger: AutoTaggingService,
                                         feedItemAcceptor: FeedItemAcceptor) {

  def acceptNewsitem(feedReaderUser: User, feednewsitem: FeedItem, feed: Feed): Future[Newsitem] = {
    val newsitem = feedItemAcceptor.acceptFeedItem(feedReaderUser: User, (feednewsitem, feed))
    val notHeld = newsitem.copy(held = false)
    contentUpdateService.create(notHeld).map { _ =>
      autoTagger.autotag(notHeld)
      contentUpdateService.update(notHeld)
      notHeld
    }
  }

}
