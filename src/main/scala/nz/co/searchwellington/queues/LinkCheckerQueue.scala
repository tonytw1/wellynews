package nz.co.searchwellington.queues

import io.micrometer.core.instrument.MeterRegistry
import nz.co.searchwellington.linkchecking.LinkCheckRequest
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import play.api.libs.json.Json

@Component
class LinkCheckerQueue @Autowired()(val rabbitConnectionFactory: RabbitConnectionFactory, val registry: MeterRegistry) {
  // Try to hide our current use of rabbitmq from the rest of the application

  private val log = LogFactory.getLog(classOf[LinkCheckerQueue])

  private val channel = rabbitConnectionFactory.connect.createChannel
  private val queuedCounter = registry.counter("linkchecker_queued")

  {
    channel.queueDeclare(LinkCheckerQueue.QUEUE_NAME, true, false, false, null)
  }

  def add(request: LinkCheckRequest): Unit = try {
    val asJson = Json.stringify(Json.toJson(request))
    log.info(s"Adding link check request to queue: $asJson")
    channel.basicPublish("", LinkCheckerQueue.QUEUE_NAME, null, asJson.getBytes)
    queuedCounter.increment()

  } catch {
    case e: Exception =>
     log.error("Failed to add to link checker queue", e)
  }

}

object LinkCheckerQueue {
  val QUEUE_NAME: String = "wellynewslinkchecker"
}
