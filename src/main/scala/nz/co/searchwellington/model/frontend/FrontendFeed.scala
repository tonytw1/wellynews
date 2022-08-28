package nz.co.searchwellington.model.frontend

import com.fasterxml.jackson.annotation.JsonFormat
import nz.co.searchwellington.model.geo.Geocode

import java.util.Date
import nz.co.searchwellington.model.{FeedAcceptancePolicy, Tag}

case class FrontendFeed(id: String,
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
                        geocode: Option[Geocode] = None,
                        held: Boolean = false,
                        latestItemDate: Date = null,
                        acceptancePolicy: FeedAcceptancePolicy = null,
                        lastRead: Option[Date] = None,
                        publisherName: Option[String] = None,
                        publisherUrlWords: Option[String] = None,
                        lastScanned: Option[Date] = None,
                        lastChanged: Option[Date] = None,
                        actions: Seq[Action] = Seq.empty
                       ) extends FrontendResource {

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  def getLatestItemDate: Date = latestItemDate

  def getAcceptancePolicy: FeedAcceptancePolicy = acceptancePolicy

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  def getLastRead: Date = lastRead.orNull

  def getPublisherName: String = publisherName.orNull
  def getPublisherUrlWords: String = publisherUrlWords.orNull

}
