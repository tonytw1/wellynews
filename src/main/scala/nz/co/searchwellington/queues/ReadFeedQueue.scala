package nz.co.searchwellington.queues

import io.micrometer.core.instrument.MeterRegistry
import nz.co.searchwellington.feeds.reading.ReadFeedRequest
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import play.api.libs.json.Json

@Component
class ReadFeedQueue @Autowired()(val rabbitConnectionFactory: RabbitConnectionFactory, val registry: MeterRegistry) {

  private val log = LogFactory.getLog(classOf[ReadFeedQueue])

  private val channel = rabbitConnectionFactory.connect.createChannel
  private val queuedCounter = registry.counter("readfeed_queued")

  {
    channel.queueDeclare(ReadFeedQueue.QUEUE_NAME, false, false, false, null)
  }

  def add(request: ReadFeedRequest): Unit = try {
    val asJson = Json.stringify(Json.toJson(request))
    log.info(s"Adding request to queue: $asJson")
    channel.basicPublish("", ReadFeedQueue.QUEUE_NAME, null, asJson.getBytes)
    queuedCounter.increment()

  } catch {
    case e: Exception =>
     log.error("Failed to add to read feed queue", e)
  }

}

object ReadFeedQueue {
  val QUEUE_NAME: String = "wellynewsreadfeed"
}
