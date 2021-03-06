package nz.co.searchwellington.feeds

import io.micrometer.core.instrument.{Counter, MeterRegistry}
import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, Resource, Tagging, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.tagging.AutoTaggingService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class FeedReaderUpdateService @Autowired()(contentUpdateService: ContentUpdateService,
                                                      autoTagger: AutoTaggingService,
                                                      feedItemAcceptor: FeedItemAcceptor,
                                                      registry: MeterRegistry) {

  private val log = Logger.getLogger(classOf[FeedReaderUpdateService])

  private lazy val acceptedCount = registry.counter("feedreader_accepted")

  def acceptFeeditem(feedReaderUser: User, feednewsitem: FeedItem, feed: Feed)(implicit ec: ExecutionContext): Future[Resource] = {
    log.info("Accepting newsitem: " + feednewsitem.url)
    val newsitem = feedItemAcceptor.acceptFeedItem(feedReaderUser: User, (feednewsitem, feed))
    log.info("Got newsitem to accept: " + newsitem)
    val notHeld = newsitem.copy(held = false)

    autoTagger.autotag(notHeld).flatMap { autoTaggings =>
      log.info("Got autotaggings: " + autoTaggings)
      val withAutoTaggings = notHeld.withTaggings(autoTaggings.map(t => Tagging(tag_id = t.tag._id, user_id = t.user._id)).toSeq)
      log.info("With autotaggings: " + withAutoTaggings)
      contentUpdateService.create(withAutoTaggings).map { created =>
        log.info("Created accepted newsitem: " + withAutoTaggings)
        acceptedCount.increment()
        created
      }
    }
  }

}
