package nz.co.searchwellington.queues

import io.micrometer.core.instrument.MeterRegistry
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class LinkCheckerQueue @Autowired()(val rabbitConnectionFactory: RabbitConnectionFactory, val registry: MeterRegistry) {
  // Try to hide our current use of rabbitmq from the rest of the application

  private val log = Logger.getLogger(classOf[LinkCheckerQueue])

  private val channel = rabbitConnectionFactory.connect.createChannel
  private val queuedCounter = registry.counter("linkchecker_queued")

  {
    channel.queueDeclare(LinkCheckerQueue.QUEUE_NAME, false, false, false, null)
  }

  def add(id: String): Unit = try {
    log.debug("Adding resource id to queue: " + id)
    channel.basicPublish("", LinkCheckerQueue.QUEUE_NAME, null, id.getBytes)
    queuedCounter.increment()
  } catch {
    case e: Exception =>
     log.error("Failed to add to link checker queue", e)
  }

}

object LinkCheckerQueue {
  val QUEUE_NAME: String = "wellynewslinkchecker"
}
