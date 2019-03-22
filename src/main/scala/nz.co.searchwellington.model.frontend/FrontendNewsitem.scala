package nz.co.searchwellington.model.frontend

import java.util.{Date, List, UUID}

import nz.co.searchwellington.model.Website
import uk.co.eelpieconsulting.common.geo.model.Place
import uk.co.eelpieconsulting.common.views.rss.RssFeedable

case class FrontendNewsitem(id: String = UUID.randomUUID().toString,
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
                            publisher: Option[Website] = None,
                            acceptedFromFeedName: String = null,
                            acceptedByProfilename: String = null,
                            accepted: Date = null,
                            image: FrontendImage = null) extends FrontendResource with RssFeedable {

  def getPublisherName: String = publisher.flatMap(_.title).orNull

  def getAcceptedFromFeedName: String = {
    return acceptedFromFeedName
  }

  def getAcceptedByProfilename: String = {
    return acceptedByProfilename
  }

  def getAccepted: Date = {
    return accepted
  }

  override def getAuthor: String = {
    return getPublisherName
  }

  def getFrontendImage: FrontendImage = {
    return image
  }

  override def getImageUrl: String = {
    return if (image != null) image.getUrl else null
  }

}
