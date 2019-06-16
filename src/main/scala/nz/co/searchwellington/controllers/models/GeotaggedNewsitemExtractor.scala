package nz.co.searchwellington.controllers.models

import nz.co.searchwellington.feeds.reading.whakaoko.model.FeedItem
import nz.co.searchwellington.model.frontend.{FrontendNewsitem, FrontendResource}
import org.springframework.stereotype.Component

@Component class GeotaggedNewsitemExtractor {

  def extractGeotaggedItems(feedNewsitems: Seq[FrontendResource]): Seq[FrontendResource] = {
    feedNewsitems.filter(_.place.nonEmpty)
  }

  def extractGeotaggedItemsFromFeedNewsitems(feedNewsitems: Seq[FeedItem]): Seq[FeedItem] = { // TODO duplication
    //feedNewsitems.filter(i => i.getPlace != null)
    Seq.empty // TODO instate
  }

}