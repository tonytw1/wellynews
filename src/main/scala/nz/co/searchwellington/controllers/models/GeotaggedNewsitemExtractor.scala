package nz.co.searchwellington.controllers.models

import nz.co.searchwellington.model.frontend.FrontendResource
import org.springframework.stereotype.Component

@Component class GeotaggedNewsitemExtractor {

  def extractGeotaggedItems(feedNewsitems: Seq[FrontendResource]): Seq[FrontendResource] = {
    feedNewsitems.filter(_.geocode.nonEmpty).reverse  // TODO pushes the most recent to last so it's rendered top of stack for overlapping points; view concern in the wrong place
  }

}