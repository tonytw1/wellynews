package nz.co.searchwellington.model.frontend

import java.util.Date

import uk.co.eelpieconsulting.common.geo.model.Place

class FrontendFeedNewsitem(name: String, url: String, date: Date, body: String,
                           place: Place, feed: FrontendFeed, publisherName: String, image: FrontendImage) extends FrontendNewsitem {

  def getFeed: FrontendFeed = {
    return feed
  }

}