package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.model.DiscoveredFeed

import java.util.Date

trait DiscoveredFeeds {

  def filterDiscoveredFeeds(discoveredFeedOccurrences: Seq[DiscoveredFeed]): Seq[(String, Date)] = {
    discoveredFeedOccurrences.groupBy(_.url).map { i =>
      (i._1, i._2.map(_.seen).min)
    }.toSeq.sortBy(_._2).reverse
  }

}
