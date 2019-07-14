package nz.co.searchwellington.feeds

import nz.co.searchwellington.feeds.reading.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, Newsitem, Tagging, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.tagging.AutoTaggingService
import org.apache.log4j.Logger
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class FeedReaderUpdateService(contentUpdateService: ContentUpdateService, autoTagger: AutoTaggingService,
                                         feedItemAcceptor: FeedItemAcceptor) {

  private val log = Logger.getLogger(classOf[FeedReaderUpdateService])

  def acceptFeeditem(feedReaderUser: User, feednewsitem: FeedItem, feed: Feed): Future[Newsitem] = {
    try {
      log.info("Accepting newsitem: " + feednewsitem.url)
      val newsitem = feedItemAcceptor.acceptFeedItem(feedReaderUser: User, (feednewsitem, feed))
      log.info("Got newsitem to accept: " + newsitem)
      val notHeld = newsitem.copy(held = false)

      autoTagger.autotag(notHeld).flatMap { autoTaggings =>
        log.info("Got autotaggins: " + autoTaggings)
        val withAutoTaggings = notHeld.withTags(autoTaggings.map(t => Tagging(tag_id = t.tag._id, user_id = t.user._id)).toSeq)
        log.info("With autotaggings: " + withAutoTaggings)
        contentUpdateService.create(withAutoTaggings).map { _ =>
          log.info("Created accepted newsitem: " + notHeld)
          withAutoTaggings
        }
      }
    }
    catch {
      case e: Exception =>
        log.error("Error while accepting feeditem", e)
        throw(e)
    }
  }

}
