package nz.co.searchwellington.model.frontend

import java.util.{Date, UUID}

import nz.co.searchwellington.model.{Tag, User, Website}
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
                            tags: Seq[Tag] = Seq.empty,
                            handTags: Seq[Tag] = Seq.empty,
                            owner: String = null,
                            place: Option[Place] = None,
                            held: Boolean = false,
                            publisher: Option[Website] = None,
                            acceptedFrom: Option[FrontendFeed] = None,
                            acceptedBy: Option[User] = None,  // TODO Frontend user
                            accepted: Date = null,
                            image: FrontendImage = null) extends FrontendResource with RssFeedable {

  def getPublisherName: String = publisher.flatMap(_.title).orNull

  def getAcceptedFromFeedName: String = acceptedFrom.map(_.name).orNull

  def getAcceptedByProfilename: String = acceptedBy.flatMap(_.profilename).orNull

  def getAccepted: Date = accepted

  override def getAuthor: String = getPublisherName

  def getFrontendImage: FrontendImage = image

  override def getImageUrl: String = if (image != null) image.getUrl else null

}
