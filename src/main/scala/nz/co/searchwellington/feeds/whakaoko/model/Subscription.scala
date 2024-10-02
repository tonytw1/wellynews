package nz.co.searchwellington.feeds.whakaoko.model

import org.joda.time.DateTime
import play.api.libs.json.{JodaReads, Json, Reads}

case class Subscription(id: String,
                        name: Option[String],
                        channelId: String,
                        url: String,
                        lastRead: Option[DateTime],
                        latestItemDate: Option[DateTime]
                       )

object Subscription {
  implicit val dr: Reads[DateTime] = JodaReads.DefaultJodaDateTimeReads
  implicit val sr: Reads[Subscription] = Json.reads[Subscription]
}