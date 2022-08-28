package nz.co.searchwellington.controllers.models

import nz.co.searchwellington.model.frontend.FrontendResource
import org.springframework.stereotype.Component

@Component class GeotaggedNewsitemExtractor {

  def extractGeotaggedItems(feedNewsitems: Seq[FrontendResource]): Seq[FrontendResource] = {
    feedNewsitems.filter(_.geocode.nonEmpty)
  }

}