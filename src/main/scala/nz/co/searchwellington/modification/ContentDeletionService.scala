package nz.co.searchwellington.modification

import nz.co.searchwellington.feeds.RssfeedNewsitemService
import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexUpdateService
import nz.co.searchwellington.repositories.{HandTaggingDAO, HibernateResourceDAO, SupressionService, TagDAO}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class ContentDeletionService @Autowired()(supressionService: SupressionService, rssfeedNewsitemService: RssfeedNewsitemService, resourceDAO: HibernateResourceDAO, handTaggingDAO: HandTaggingDAO, tagDAO: TagDAO, elasticSearchIndexUpdateService: ElasticSearchIndexUpdateService) {

  private val log = Logger.getLogger(classOf[ContentDeletionService])

  def performDelete(resource: Resource) {
    handTaggingDAO.clearTags(resource)
    if (resource.`type` == "W") {
      removePublisherFromPublishersContent(resource)
    }
    if (resource.`type` == "F") {
      removeFeedFromFeedNewsitems(resource.asInstanceOf[Feed])
      removeRelatedFeedFromTags(resource.asInstanceOf[Feed])
    }
    if (resource.`type` == "N") {
      log.info("Deleted item is a newsitem; checking if it's in an accepted feed.")
      val deletedNewsitem: Newsitem = resource.asInstanceOf[Newsitem]

      deletedNewsitem.page.map { p =>
        if (rssfeedNewsitemService.isUrlInAcceptedFeeds(p)) {
          log.info("Supressing deleted newsitem url as it still visible in an automatically deleted feed: " + p)
          suppressUrl(p)
        } else {
          log.info("Not found in live feeds; not supressing")
        }
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

  private def suppressUrl(p: String) {
    log.info("Deleting a newsitem whose url still appears in a feed; suppressing the url: " + p)
    supressionService.suppressUrl(p)
  }

  private def removePublisherFromPublishersContent(editResource: Resource) {
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
  }

  private def removeFeedFromFeedNewsitems(feed: Feed) {
    resourceDAO.getNewsitemsForFeed(feed).map { newsitem =>
      // newsitem.setFeed(null)
      resourceDAO.saveResource(newsitem)
    }
  }

}
