package nz.co.searchwellington.modification

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.RssfeedNewsitemService
import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexer
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{HandTaggingDAO, SupressionService, TagDAO}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.api.commands.UpdateWriteResult

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

@Component class ContentDeletionService @Autowired()(supressionService: SupressionService,
                                                     rssfeedNewsitemService: RssfeedNewsitemService,
                                                     mongoRepository: MongoRepository, handTaggingDAO: HandTaggingDAO,
                                                     tagDAO: TagDAO, elasticSearchIndexer: ElasticSearchIndexer)
  extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[ContentDeletionService])

  def performDelete(resource: Resource): Unit = {
    handTaggingDAO.clearTags(resource)  // TODO does nothing
    if (resource.`type` == "W") {
      removePublisherFromPublishersContent(resource)
    }
    if (resource.`type` == "F") {
      Await.result(removeFeedFromFeedNewsitems(resource.asInstanceOf[Feed]), OneMinute)
    }
    if (resource.`type` == "N") {
      log.info("Deleted item is a newsitem; checking if it's in an accepted feed.")
      val deletedNewsitem = resource.asInstanceOf[Newsitem]

      deletedNewsitem.page.map { p =>
        if (rssfeedNewsitemService.isUrlInAcceptedFeeds(p)) {
          log.info("Supressing deleted newsitem url as it still visible in an automatically accepted feed: " + p)
          suppressUrl(p)
        } else {
          log.info("Not found in live feeds; not supressing")
        }
      }
    }

    Await.result(elasticSearchIndexer.deleteResource(resource._id).flatMap { dr =>
      log.info("Elastic delete result: " + dr)
      mongoRepository.removeResource(resource)
    }, TenSeconds)
  }

  private def suppressUrl(p: String) {
    log.info("Deleting a newsitem whose url still appears in a feed; suppressing the url: " + p)
    supressionService.suppressUrl(p)
  }

  private def removePublisherFromPublishersContent(editResource: Resource) {
    throw new UnsupportedOperationException
    /*
    val publisher = editResource.asInstanceOf[Website]
    resourceDAO.getNewsitemsForPublishers(publisher).map { published =>
      // published.setPublisher(null)
      resourceDAO.saveResource(publisher)
    }
    for (feed <- publisher.getFeeds) {
      // feed.setPublisher(null)
      resourceDAO.saveResource(feed)
    }
    for (watchlist <- publisher.getWatchlist) {
      // watchlist.setPublisher(null)
      resourceDAO.saveResource(watchlist)
    }
    */
  }

  private def removeFeedFromFeedNewsitems(feed: Feed): Future[Seq[UpdateWriteResult]] = {
    mongoRepository.getAllNewsitemsForFeed(feed).flatMap { ns =>
      Future.sequence {
        ns.map { n =>
          log.info("Removing feed from newsitem: " + n.title)
          mongoRepository.saveResource(n.copy(feed = None))
        }
      }
    }
  }

}
