package nz.co.searchwellington.model.frontend

import java.util.Date

import uk.co.eelpieconsulting.common.geo.model.Place

class FrontendFeedNewsitem(name: String, url: String, date: Date, body: String,
                           place: Place, feed: FrontendFeed, publisherName: String, image: FrontendImage) extends FrontendNewsitem {

  private var suppressed: Boolean = false
  private var localCopy: Integer = null

  def getFeed: FrontendFeed = {
    return feed
  }

  override def getFrontendImage: FrontendImage = {
    return image
  }

  def getLocalCopy: Integer = {
    return localCopy
  }

  def isSuppressed: Boolean = {
    this.suppressed
  }

}