package nz.co.searchwellington.model.frontend

import java.util.{Date, List}

import nz.co.searchwellington.model.Comment
import uk.co.eelpieconsulting.common.geo.model.Place
import uk.co.eelpieconsulting.common.views.rss.RssFeedable

case class FrontendNewsitem(id: Int = 0,
                            urlWords: String = null,
                            `type`: String = null,
                            name: String = null,
                            url: String = null,
                            httpStatus: Int = 0,
                            date: Date = null,
                            description: String = null,
                            liveTime: Date = null,
                            tags: List[FrontendTag] = null,
                            handTags: List[FrontendTag] = null,
                            owner: String = null,
                            place: Place = null,
                            held: Boolean = false,
                            publisherName: String,
                            acceptedFromFeedName: String = null,
                            acceptedByProfilename: String = null,
                            comments: List[Comment] = null,
                            accepted: Date = null,
                            image: FrontendImage = null) extends FrontendResource with RssFeedable {

  def getAcceptedFromFeedName: String = {
    return acceptedFromFeedName
  }

  def getComments: List[Comment] = {
    return comments
  }

  def getAcceptedByProfilename: String = {
    return acceptedByProfilename
  }

  def getAccepted: Date = {
    return accepted
  }

  override def getAuthor: String = {
    return publisherName
  }

  def getFrontendImage: FrontendImage = {
    return image
  }

  override def getImageUrl: String = {
    return if (image != null) image.getUrl else null
  }

}
