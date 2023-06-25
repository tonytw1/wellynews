package nz.co.searchwellington.feeds.reading

import nz.co.searchwellington.model.FeedAcceptancePolicy
import play.api.libs.json.{Json, Reads, Writes}

import java.util.Date

case class ReadFeedRequest(feedId: String, asUserId: String, acceptedPolicy: Option[String], lastRead: Option[Date])

object ReadFeedRequest {
  implicit val r: Reads[ReadFeedRequest] = Json.reads[ReadFeedRequest]
  implicit val w: Writes[ReadFeedRequest] = Json.writes[ReadFeedRequest]
}
