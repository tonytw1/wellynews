package nz.co.searchwellington.feeds.whakaoko.model

import org.joda.time.DateTime
import play.api.libs.json.{JodaReads, Json, Reads}

import java.util.Date

case class FeedItem(id: String,
                    title: Option[String] = None,
                    body: Option[String] = None,
                    subscriptionId: String,
                    url: String,
                    imageUrl: Option[String] = None,
                    date: Option[Date] = None,
                    place: Option[Place] = None,
                    categories: Option[Seq[Category]] = None,
                    accepted: Option[DateTime] = None
                   )

object FeedItem {
  implicit val dr: Reads[DateTime] = JodaReads.DefaultJodaDateTimeReads
  implicit val llr: Reads[LatLong] = Json.reads[LatLong]
  implicit val pr: Reads[Place] = Json.reads[Place]
  implicit val cr: Reads[Category] = Json.reads[Category]
  implicit val fir: Reads[FeedItem] = Json.reads[FeedItem]
}