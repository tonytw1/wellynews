package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.{Feed, Newsitem, Tagging, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.tagging.AutoTaggingService
import org.apache.log4j.Logger
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class FeedReaderUpdateService(contentUpdateService: ContentUpdateService, autoTagger: AutoTaggingService,
                                         feedItemAcceptor: FeedItemAcceptor) {

  private val log = Logger.getLogger(classOf[AutoTaggingService])

  def acceptNewsitem(feedReaderUser: User, feednewsitem: FeedItem, feed: Feed): Future[Newsitem] = {
    val newsitem = feedItemAcceptor.acceptFeedItem(feedReaderUser: User, (feednewsitem, feed))
    val notHeld = newsitem.copy(held = false)
    val autoTaggings = autoTagger.autotag(notHeld)
    val autoTagged = notHeld.copy(resource_tags = autoTaggings.map(t => Tagging(tag_id = t.tag._id, user_id = t.user._id)).toSeq)
    contentUpdateService.create(autoTagged).map { _ =>
      log.info("Created accepted newsitem: " + notHeld)
      autoTagged
    }
  }

}
