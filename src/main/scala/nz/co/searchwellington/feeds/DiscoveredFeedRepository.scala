package nz.co.searchwellington.feeds

import nz.co.searchwellington.commentfeeds.CommentFeedDetectorService
import nz.co.searchwellington.model.DiscoveredFeed
import nz.co.searchwellington.repositories.HibernateResourceDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class DiscoveredFeedRepository @Autowired() (var resourceDAO: HibernateResourceDAO, var commentFeedDetectorService: CommentFeedDetectorService) {

  def getAllNonCommentDiscoveredFeeds: Seq[DiscoveredFeed] = {
    resourceDAO.getAllDiscoveredFeeds.filter{ f =>
      !commentFeedDetectorService.isCommentFeedUrl(f.url)
    }
  }

}