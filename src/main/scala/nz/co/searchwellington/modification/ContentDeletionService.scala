package nz.co.searchwellington.modification

import nz.co.searchwellington.feeds.RssfeedNewsitemService
import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.HandTaggingDAO
import nz.co.searchwellington.repositories.HibernateResourceDAO
import nz.co.searchwellington.repositories.SupressionService
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexUpdateService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component class ContentDeletionService @Autowired()(supressionService: SupressionService, rssfeedNewsitemService: RssfeedNewsitemService, resourceDAO: HibernateResourceDAO, handTaggingDAO: HandTaggingDAO, tagDAO: TagDAO, elasticSearchIndexUpdateService: ElasticSearchIndexUpdateService) {

  private val log = Logger.getLogger(classOf[ContentDeletionService])

  @Transactional def performDelete(resource: Resource) {
    handTaggingDAO.clearTags(resource)
    if (resource.getType == "W") {
      removePublisherFromPublishersContent(resource)
    }
    if (resource.getType == "F") {
      removeFeedFromFeedNewsitems(resource.asInstanceOf[Feed])
      removeRelatedFeedFromTags(resource.asInstanceOf[Feed])
    }
    if (resource.getType == "N") {
      log.info("Deleted item is a newsitem; checking if it's in an accepted feed.")
      val deletedNewsitem: Newsitem = resource.asInstanceOf[Newsitem]
      if (rssfeedNewsitemService.isUrlInAcceptedFeeds(deletedNewsitem.getUrl)) {
        log.info("Supressing deleted newsitem url as it still visible in an automatically deleted feed: " + deletedNewsitem.getUrl)
        suppressDeletedNewsitem(deletedNewsitem)
      }
      else {
        log.info("Not found in live feeds; not supressing")
      }
    }
    //elasticSearchIndexUpdateService.deleteContentItem(resource.getId)
    resourceDAO.deleteResource(resource)
  }

  private def removeRelatedFeedFromTags(editResource: Feed) {
    tagDAO.getAllTags.map { tag =>
      // TODO if (tag.getRelatedFeed != null && tag.getRelatedFeed == editResource) {
      //  tag.setRelatedFeed(null)
      //}
    }
  }

  private def suppressDeletedNewsitem(deletedNewsitem: Newsitem) {
    log.info("Deleting a newsitem whose url still appears in a feed; suppressing the url: " + deletedNewsitem.getUrl)
    supressionService.suppressUrl(deletedNewsitem.getUrl)
  }

  private def removePublisherFromPublishersContent(editResource: Resource) {
    val publisher = editResource.asInstanceOf[Website]
    resourceDAO.getNewsitemsForPublishers(publisher).map { published =>
      // published.setPublisher(null)
      resourceDAO.saveResource(publisher)
    }

    import scala.collection.JavaConversions._
    for (feed <- publisher.getFeeds) {
      // feed.setPublisher(null)
      resourceDAO.saveResource(feed)
    }
    import scala.collection.JavaConversions._
    for (watchlist <- publisher.getWatchlist) {
      // watchlist.setPublisher(null)
      resourceDAO.saveResource(watchlist)
    }
  }

  private def removeFeedFromFeedNewsitems(feed: Feed) {
    resourceDAO.getNewsitemsForFeed(feed).map { newsitem =>
      // newsitem.setFeed(null)
      resourceDAO.saveResource(newsitem)
    }
  }

}
