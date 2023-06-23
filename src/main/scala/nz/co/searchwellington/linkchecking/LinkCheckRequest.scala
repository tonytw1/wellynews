package nz.co.searchwellington.linkchecking

import play.api.libs.json.{Json, Reads, Writes}

import java.util.Date

case class LinkCheckRequest(resourceId: String, lastScanned: Option[Date])

object LinkCheckRequest {
  implicit val lcrr: Reads[LinkCheckRequest] = Json.reads[LinkCheckRequest]
  implicit val lcrw: Writes[LinkCheckRequest] = Json.writes[LinkCheckRequest]
}
