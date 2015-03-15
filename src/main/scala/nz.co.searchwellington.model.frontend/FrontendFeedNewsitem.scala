package nz.co.searchwellington.model.frontend

import java.util.Date

import uk.co.eelpieconsulting.common.geo.model.Place

class FrontendFeedNewsitem(name: String, url: String, date: Date, description: String,
                           place: Place, feed: FrontendFeed, publisherName: String, image: FrontendImage) extends FrontendNewsitem() {
  this.setName(name)
  this.setUrl(url)
  this.setDate(date)
  this.setDescription(description)
  this.setPlace(place)
  this.setPublisherName(publisherName)
  this.setFrontendImage(image)
  this.setType("FNI")
  
  def getFeed: FrontendFeed = {
    return feed
  }

}