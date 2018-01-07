package nz.co.searchwellington.controllers.models

import com.google.common.collect.Lists
import nz.co.searchwellington.model.frontend.{FrontendFeedNewsitem, FrontendNewsitem}
import org.springframework.stereotype.Component

@Component class GeotaggedNewsitemExtractor {

  def extractGeotaggedItems(feedNewsitems: util.List[FrontendNewsitem]): util.List[FrontendNewsitem] = {
    val geotaggedFeedNewsitems: util.List[FrontendNewsitem] = Lists.newArrayList
    for (feedNewsitem <- feedNewsitems) {
      if (feedNewsitem.getPlace != null) {
        geotaggedFeedNewsitems.add(feedNewsitem)
      }
    }
    return geotaggedFeedNewsitems
  }

  def extractGeotaggedItemsFromFeedNewsitems(feedNewsitems: util.List[FrontendFeedNewsitem]): util.List[FrontendNewsitem] = {
    val geotaggedFeedNewsitems: util.List[FrontendNewsitem] = Lists.newArrayList
    for (feedNewsitem <- feedNewsitems) {
      if (feedNewsitem.getPlace != null) {
        geotaggedFeedNewsitems.add(feedNewsitem)
      }
    }
    return geotaggedFeedNewsitems
  }
}