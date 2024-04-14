package nz.co.searchwellington.queues

import io.micrometer.core.instrument.MeterRegistry
import nz.co.searchwellington.linkchecking.LinkCheckRequest
import nz.co.searchwellington.model.Resource
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import play.api.libs.json.Json
import reactivemongo.api.bson.BSONObjectID

@Component
class ElasticIndexQueue @Autowired()(val rabbitConnectionFactory: RabbitConnectionFactory, val registry: MeterRegistry) {
  // Try to hide our current use of rabbitmq from the rest of the application

  private val log = LogFactory.getLog(classOf[LinkCheckerQueue])

  private val channel = rabbitConnectionFactory.connect.createChannel
  private val queuedCounter = registry.counter("elasticindex_queued")

  {
    channel.queueDeclare(ElasticIndexQueue.QUEUE_NAME, true, false, false, null)
  }

  def add(resource: Resource): Boolean = try {
    add(resource._id)
  }

  def add(resourceId: BSONObjectID): Boolean = try {
    val request = ElasticIndexRequest(resourceId = resourceId.stringify)
    val asJson = Json.stringify(Json.toJson(request))

    log.info(s"Adding elastic index request to queue: $asJson")
    channel.basicPublish("", ElasticIndexQueue.QUEUE_NAME, null, asJson.getBytes)
    queuedCounter.increment()
    true
  } catch {
    case e: Exception =>
      log.error("Failed to add to  elastic index queue", e)
      false
  }

}

object ElasticIndexQueue {
  val QUEUE_NAME: String = "wellynewselasticindex"
}
