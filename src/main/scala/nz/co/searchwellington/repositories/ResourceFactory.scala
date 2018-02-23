package nz.co.searchwellington.repositories

import com.google.common.collect.Lists
import nz.co.searchwellington.model._
import org.springframework.stereotype.Component
import java.util.Calendar
import java.util

@Component class ResourceFactory {
  def createNewNewsitem = Newsitem()

  def createNewWebsite = Website()

  def createNewFeed = Feed()

  def createNewWatchlist = Watchlist()

  def createNewCommentFeed(commentFeedUrl: String) = new CommentFeed(0, commentFeedUrl, Lists.newArrayList[Comment], null, null)

  def createNewDiscoveredFeed(discoveredUrl: String): DiscoveredFeed = {
    val discoveredFeed = new DiscoveredFeed
    discoveredFeed.setUrl(discoveredUrl)
    discoveredFeed.setReferences(new util.HashSet[Resource])
    discoveredFeed
  }
}
