package nz.co.searchwellington.controllers.models

import nz.co.searchwellington.model.frontend.FrontendNewsitem
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Component class GeotaggedNewsitemExtractor {

  def extractGeotaggedItems(feedNewsitems: Seq[FrontendNewsitem]): Seq[FrontendNewsitem] = {
    feedNewsitems.filter(i => i.getPlace != null)
  }

  def extractGeotaggedItemsFromFeedNewsitems(feedNewsitems: Seq[FeedItem]): Seq[FeedItem] = { // TODO duplication
    feedNewsitems.filter(i => i.getPlace != null)
  }

}