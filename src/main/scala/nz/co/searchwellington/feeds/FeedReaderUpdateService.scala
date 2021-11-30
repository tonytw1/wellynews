package nz.co.searchwellington.feeds

import io.micrometer.core.instrument.MeterRegistry
import nz.co.searchwellington.feeds.whakaoko.model.Category
import nz.co.searchwellington.model._
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

  def acceptFeeditem(feedReaderUser: User, feednewsitem: Newsitem, feed: Feed, feedItemCategories: Seq[Category])(implicit ec: ExecutionContext): Future[Resource] = {
    log.info("Accepting newsitem: " + feednewsitem.page)
    val newsitem = feedItemAcceptor.acceptFeedItem(feedReaderUser: User, (feednewsitem, feed))
    log.info("Got newsitem to accept: " + newsitem)
    val notHeld = newsitem.copy(held = false)

    if (feedItemCategories.nonEmpty) {
      log.info("Saw a feed item with RSS categories; we should use these as an autotagging signal: " + feedItemCategories.map(_.value).mkString(","))
    }

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
