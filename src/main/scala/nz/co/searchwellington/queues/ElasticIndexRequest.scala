package nz.co.searchwellington.queues

import play.api.libs.json.{Json, Reads, Writes}

case class ElasticIndexRequest(resourceId: String)

object ElasticIndexRequest {
  implicit val eirr: Reads[ElasticIndexRequest] = Json.reads[ElasticIndexRequest]
  implicit val eirw: Writes[ElasticIndexRequest] = Json.writes[ElasticIndexRequest]
}