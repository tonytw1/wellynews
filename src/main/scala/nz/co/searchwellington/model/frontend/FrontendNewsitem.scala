package nz.co.searchwellington.model.frontend

import com.fasterxml.jackson.annotation.JsonFormat

import java.util
import java.util.Date
import nz.co.searchwellington.model.{Geocode, Tag, User, Website}
import uk.co.eelpieconsulting.common.views.rss.RssFeedable

case class FrontendNewsitem(id: String,
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
                            place: Option[Geocode] = None,
                            held: Boolean = false,
                            publisher: Option[Website] = None,  // TODO why not a frontend website
                            acceptedFrom: Option[FrontendFeed] = None,
                            acceptedBy: Option[User] = None, // TODO Frontend user
                            accepted: Date = null,
                            image: FrontendImage = null,
                            lastScanned: Option[Date] = None,
                            lastChanged: Option[Date] = None,
                            actions: Seq[Action] = Seq.empty) extends FrontendResource with RssFeedable {

  def getPublisherName: String = publisher.flatMap(_.title).orNull

  def getAcceptedFromFeedName: String = acceptedFrom.map(_.name).orNull

  def getAcceptedByProfilename: String = acceptedBy.flatMap(_.profilename).orNull

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  def getAccepted: Date = accepted

  override def getAuthor: String = getPublisherName

  def getFrontendImage: FrontendImage = image

  override def getImageUrl: String = if (image != null) image.getUrl else null

  def getHangTags: util.List[Tag] = {
    import scala.collection.JavaConverters._
    handTags.asJava
  }

}
