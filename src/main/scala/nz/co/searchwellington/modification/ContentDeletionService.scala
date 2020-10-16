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

@Component class ContentDeletionService @Autowired()(suppressionDAO: SuppressionDAO,
                                                     mongoRepository: MongoRepository,
                                                     handTaggingDAO: HandTaggingDAO,
                                                     elasticSearchIndexer: ElasticSearchIndexer,
                                                     contentUpdateService: ContentUpdateService)
  extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[ContentDeletionService])

  def performDelete(resource: Resource): Unit = {
    try {
      log.info("Deleting resource: " + resource)
      handTaggingDAO.clearTags(resource) // TODO does nothing

      resource match {
        case website: Website =>
          removePublisherFromPublishersContent(website)
        case feed: Feed =>
          Await.result(removeFeedFromFeedNewsitems(feed), OneMinute)
        case newsitem: Newsitem =>
          log.info("Suppressing deleted newsitem url to prevent it been reaccepted from a feed: " + newsitem.page)
          suppressionDAO.addSuppression(newsitem.page)
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

  private def removePublisherFromPublishersContent(publisher: Website) {
    mongoRepository.getNewsitemIdsForPublisher(publisher).map { published =>
      published.foreach { publishedResource =>
        mongoRepository.getResourceByObjectId(publishedResource).map {
          case published: PublishedResource =>
            log.info("Clearing publisher from: " + published.title)
            published.setPublisher(null)  // TODO executes a Some(None)
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
