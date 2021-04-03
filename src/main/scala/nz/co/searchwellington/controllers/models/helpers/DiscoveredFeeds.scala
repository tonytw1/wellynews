package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.model.DiscoveredFeed

trait DiscoveredFeeds {

  def filterDiscoveredFeeds(discoveredFeedOccurrences: Seq[DiscoveredFeed]): Seq[(String, DiscoveredFeed)] = {
    discoveredFeedOccurrences.groupBy(_.url).map { i =>
      (i._1, i._2.minBy(_.seen))
    }.toSeq.sortBy(_._2.seen).reverse
  }

}
