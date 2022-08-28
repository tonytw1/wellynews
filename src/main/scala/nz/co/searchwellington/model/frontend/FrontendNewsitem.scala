package nz.co.searchwellington.model.frontend

import com.fasterxml.jackson.annotation.JsonFormat
import nz.co.searchwellington.model.geo.Geocode
import nz.co.searchwellington.model.{Tag, User}
import uk.co.eelpieconsulting.common.views.rss.RssFeedable

import java.util
import java.util.Date
import scala.jdk.CollectionConverters._

case class FrontendNewsitem(id: String,
                            urlWords: String = null,
                            `type`: String = null,
                            name: String = null,
                            url: String = null,
                            httpStatus: Int = 0,
                            date: Date = null,
                            description: String = null,
                            liveTime: Date = null,
                            tags: Option[Seq[Tag]] = Some(Seq.empty),
                            handTags: Option[Seq[Tag]] = Some(Seq.empty),
                            owner: String = null,
                            place: Option[Geocode] = None,
                            held: Boolean = false,
                            publisherName: Option[String] = None,
                            publisherUrlWords: Option[String] = None,
                            acceptedFrom: Option[FrontendFeed] = None,
                            acceptedBy: Option[User] = None, // TODO Frontend user
                            accepted: Date = null,
                            image: FrontendImage = null,
                            lastScanned: Option[Date] = None,
                            lastChanged: Option[Date] = None,
                            actions: Seq[Action] = Seq.empty) extends FrontendResource with RssFeedable {

  def getPublisherName: String = publisherName.orNull
  def getPublisherUrlWords: String = publisherUrlWords.orNull

  def getAcceptedFromFeedName: String = acceptedFrom.map(_.name).orNull

  def getAcceptedByProfilename: String = acceptedBy.flatMap(_.profilename).orNull

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  def getAccepted: Date = accepted

  override def getAuthor: String = getPublisherName

  def getFrontendImage: FrontendImage = image

  override def getImageUrl: String = if (image != null) image.getUrl else null

  def getHangTags: util.List[Tag] = {
    handTags.map(_.asJava).orNull
  }

}
