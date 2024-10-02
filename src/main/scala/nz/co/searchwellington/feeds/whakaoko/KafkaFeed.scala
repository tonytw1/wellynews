package nz.co.searchwellington.feeds.whakaoko

import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import org.apache.commons.logging.LogFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import play.api.libs.json.Json

@Component
class KafkaFeed {

  val log = LogFactory.getLog(classOf[KafkaFeed])

  @KafkaListener(topics = Array("whakaoko.wellynews"))
  def listen(message: String): Unit = {
    log.info(s"Got message: " + message)

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
