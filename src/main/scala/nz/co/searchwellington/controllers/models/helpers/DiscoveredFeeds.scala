package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.model.DiscoveredFeed

trait DiscoveredFeeds {

  def filterDiscoveredFeeds(discoveredFeedOccurrences: Seq[DiscoveredFeed]): Seq[(String, DiscoveredFeed)] = {
    discoveredFeedOccurrences.groupBy(_.url).map { i =>
      val feeds: Seq[DiscoveredFeed] = i._2.sortBy(_.seen)
      (i._1, feeds.head)
    }.toSeq.sortBy(_._2.seen).reverse
  }

}
