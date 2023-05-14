package nz.co.searchwellington.feeds

import io.micrometer.core.instrument.MeterRegistry
import nz.co.searchwellington.feeds.whakaoko.model.{Category, FeedItem}
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.taggingvotes.HandTagging
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.tagging.AutoTaggingService
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class FeedReaderUpdateService @Autowired()(contentUpdateService: ContentUpdateService,
                                                      autoTagger: AutoTaggingService,
                                                      feedItemAcceptor: FeedItemAcceptor,
                                                      registry: MeterRegistry) {

  private val log = LogFactory.getLog(classOf[FeedReaderUpdateService])

  private lazy val acceptedCount = registry.counter("feedreader_accepted") // Wrong name all; all user use this


  def acceptFeeditem(user: User, feednewsitem: FeedItem, feed: Feed, feedItemCategories: Seq[Category])(implicit ec: ExecutionContext): Future[Option[Resource]] = {
    // Given a feed item from a feed, accept it and create a newsitem as the call user
    // Then apply auto tagging
    log.info("Accepting feed item: " + feednewsitem.title + " from feed: " + feed.title)

    feedItemAcceptor.acceptFeedItem(user: User, (feednewsitem, feed)).map { newsitem =>
      log.info("Got acceptable news item: " + newsitem)
      val notHeld = newsitem.copy(held = false)

      val eventualAutoTaggings = autoTagger.autotag(notHeld)
      val eventualFeedCategoryAutoTaggings = autoTagger.autoTagsForFeedCategories(feedItemCategories)

      val withAutoTaggings = for {
        autoTaggings <- eventualAutoTaggings
        feedCategoryAutoTaggings <- eventualFeedCategoryAutoTaggings
        withAutoTaggings <- {
          log.info("Got autotaggings: " + asCommaListOfTagIds(autoTaggings) + " for news item: " + feednewsitem.title)
          if (feedCategoryAutoTaggings.nonEmpty) {
            log.info("Got feed info category auto taggings: " + asCommaListOfTagIds(feedCategoryAutoTaggings) + " from feed news item categories: "
              + feedItemCategories.map(_.value).mkString(","))
          }
          val allTaggings: Set[HandTagging] = autoTaggings ++ feedCategoryAutoTaggings

          val withAutoTaggings = notHeld.withTaggings(allTaggings.map(t =>
            Tagging(tag_id = t.tag._id, user_id = t.taggingUser._id, reason = t.reason)).toSeq)

          contentUpdateService.create(withAutoTaggings).map { created =>
            log.info("Created accepted newsitem: " + withAutoTaggings)
            acceptedCount.increment()
            created
          }
        }
      } yield {
        Some(withAutoTaggings)
      }
      withAutoTaggings

    }.getOrElse {
      Future.successful(None)
    }
  }

  private def asCommaListOfTagIds(tags: Set[HandTagging]): String = tags.map(_.tag.id).mkString(",")

}
