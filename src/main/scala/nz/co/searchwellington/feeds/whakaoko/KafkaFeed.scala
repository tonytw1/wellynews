package nz.co.searchwellington.feeds.whakaoko

import nz.co.searchwellington.feeds.whakaoko.model.{Category, FeedItem, LatLong, Place, Subscription}
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import play.api.libs.json.{JodaReads, Json, Reads}

@Component
class KafkaFeed {

  val log = LogFactory.getLog(classOf[KafkaFeed])

  @KafkaListener(topics = Array("whakaoko.wellynews"))
  def listen(message: String): Unit = {
    log.info(s"Got message: " + message)

    implicit val dr: Reads[DateTime] = JodaReads.DefaultJodaDateTimeReads
    implicit val llr: Reads[LatLong] = Json.reads[LatLong]
    implicit val pr: Reads[Place] = Json.reads[Place]
    implicit val cr: Reads[Category] = Json.reads[Category]
    implicit val fir: Reads[FeedItem] = Json.reads[FeedItem]

    // Should be able to deserialize this as feed item
    try {
      val feedItem = Json.parse(message).as[FeedItem]
      log.info(s"Parsed feed item from message: $feedItem")
    } catch {
      case e: Exception =>
        log.warn(s"Failed to parse feed item message: ${e.getMessage} / $message")
    }
  }

}
