package nz.co.searchwellington.controllers.models

import nz.co.searchwellington.model.frontend.{FrontendFeedNewsitem, FrontendNewsitem}
import org.springframework.stereotype.Component

@Component class GeotaggedNewsitemExtractor {

  def extractGeotaggedItems(feedNewsitems: Seq[FrontendNewsitem]): Seq[FrontendNewsitem] = {
    feedNewsitems.filter(i => i.getPlace != null)
  }

  def extractGeotaggedItemsFromFeedNewsitems(feedNewsitems: Seq[FrontendFeedNewsitem]): Seq[FrontendNewsitem] = { // TODO duplication
    feedNewsitems.filter(i => i.getPlace != null)
  }

}