package nz.co.searchwellington.modification

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.RssfeedNewsitemService
import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexer
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{HandTaggingDAO, SuppressionDAO, TagDAO}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Component class ContentDeletionService @Autowired()(suppressionService: SuppressionDAO,
                                                     rssfeedNewsitemService: RssfeedNewsitemService,
                                                     mongoRepository: MongoRepository, handTaggingDAO: HandTaggingDAO,
                                                     tagDAO: TagDAO, elasticSearchIndexer: ElasticSearchIndexer,
                                                     contentUpdateService: ContentUpdateService)
  extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[ContentDeletionService])

  def performDelete(resource: Resource): Unit = {
    try {
      handTaggingDAO.clearTags(resource) // TODO does nothing

      resource match {
        case website: Website =>
          removePublisherFromPublishersContent(website)
        case feed: Feed =>
          Await.result(removeFeedFromFeedNewsitems(feed), OneMinute)
        case newsitem: Newsitem =>
          log.info("Deleted item is a newsitem; checking if it's in an accepted feed.")
          if (Await.result(rssfeedNewsitemService.isUrlInAcceptedFeeds(newsitem.page), TenSeconds)) {
            log.info("Supressing deleted newsitem url as it still visible in an automatically accepted feed: " + newsitem.page)
            suppressUrl(newsitem.page)
          } else {
            log.info("Not found in live feeds; not suppressing")
          }

      }

      Await.result(elasticSearchIndexer.deleteResource(resource._id).flatMap { dr =>
        log.info("Elastic delete result: " + dr)
        mongoRepository.removeResource(resource)
      }, TenSeconds)

    } catch {
      case e: Exception =>
        log.error("Delete error: " + e.getMessage, e)
    }
  }

  private def suppressUrl(p: String) {
    log.info("Deleting a newsitem whose url still appears in a feed; suppressing the url: " + p)
    suppressionService.addSuppression(p)
  }

  private def removePublisherFromPublishersContent(publisher: Website) {
    mongoRepository.getNewsitemIdsForPublisher(publisher).map { published =>
      published.foreach { publishedResource =>
        mongoRepository.getResourceByObjectId(publishedResource).map {
          case published: PublishedResource =>
            log.info("Clearing publisher from: " + published.title)
            published.setPublisher(null)
            contentUpdateService.update(published)
        }
      }
    }
  }

  private def removeFeedFromFeedNewsitems(feed: Feed): Future[Seq[Boolean]] = {
    mongoRepository.getAllNewsitemsForFeed(feed).flatMap { ns =>
      Future.sequence {
        ns.map { n =>
          log.info("Removing feed from newsitem: " + n.title)
          val withoutFeed = n.copy(feed = None)
          contentUpdateService.update(withoutFeed)
        }
      }
    }
  }

}
