package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.model.DiscoveredFeed

trait DiscoveredFeeds {

  def filterDiscoveredFeeds(discoveredFeedOccurrences: Seq[DiscoveredFeed]): Seq[DiscoveredFeed] = {
    discoveredFeedOccurrences // TODO deprecate with better modeling
  }

}
