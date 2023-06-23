package nz.co.searchwellington.feeds.reading

import play.api.libs.json.{Json, Reads, Writes}

import java.util.Date

case class ReadFeedRequest(feedId: String, lastRead: Option[Date])

object ReadFeedRequest {
  implicit val r: Reads[ReadFeedRequest] = Json.reads[ReadFeedRequest]
  implicit val w: Writes[ReadFeedRequest] = Json.writes[ReadFeedRequest]
}
