package nz.co.searchwellington.feeds.whakaoko

import org.apache.commons.logging.LogFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class KafkaFeed {

  private val log = LogFactory.getLog(classOf[KafkaFeed])

  @KafkaListener(topics = Array("test"))
  def listen(message: String): Unit = {
    log.info(s"Got message: " + message)
  }

}
