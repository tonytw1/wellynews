package nz.co.searchwellington.repositories.elasticsearch

import com.rabbitmq.client.{CancelCallback, Channel, DeliverCallback, Delivery}
import io.micrometer.core.instrument.MeterRegistry
import nz.co.searchwellington.queues.{ElasticIndexQueue, ElasticIndexRequest, RabbitConnectionFactory}
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Component
import play.api.libs.json.Json
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

@Component
class ElasticIndexConsumer @Autowired()(elasticSearchIndexRebuildService: ElasticSearchIndexRebuildService,
                                        rabbitConnectionFactory: RabbitConnectionFactory,
                                        @Qualifier("elasticIndexTaskExecutor") taskExecutor: TaskExecutor,
                                        registry: MeterRegistry) {

  private val log = LogFactory.getLog(classOf[ElasticIndexConsumer])

  private val pulledCounter = registry.counter("elasticindex_pulled")

  private val maximumConcurrentChecks = 1

  {
    log.info("Starting elastic index listener")
    try {
      val connection = rabbitConnectionFactory.connect

      val channel = connection.createChannel
      // The consumer immediately dispatches each new message into a Future.
      // There is no back pressure from the consumer to stop Rabbit flooding us.
      // So we'll use the Rabbit channel maximum unacked messages / Qos as our flow control.
      channel.basicQos(maximumConcurrentChecks)

      implicit val executionContext: ExecutionContextExecutor = ExecutionContext.fromExecutor(taskExecutor)

      val deliverCallback: DeliverCallback = (consumerTag: String, message: Delivery) => {
        try {
          log.debug(s"Elastic index handling delivery with consumer tag: $consumerTag")
          pulledCounter.increment()
          val body = message.getBody
          val asJson = new String(body)
          val request = Json.parse(asJson).as[ElasticIndexRequest]

          val resourceId = BSONObjectID.parse(request.resourceId).get

          val toIndex = Seq(resourceId)
          elasticSearchIndexRebuildService.reindexResources(resourcesToIndex = toIndex, totalResources = toIndex.size).map { r: Int =>
            channel.basicAck(message.getEnvelope.getDeliveryTag, false)
            logQueueCount(channel)

          }.recover {
            case e: Throwable => log.error(s"Index future failed; ignoring and acking", e)
            channel.basicAck(message.getEnvelope.getDeliveryTag, false)
          }

        } catch {
          case e: Exception =>
            log.error("Error while processing elastic index message; ignoring and acking", e)
            channel.basicAck(message.getEnvelope.getDeliveryTag, false)
        }
      }

      val cancelCallback: CancelCallback = (consumerTag: String) => {
        log.info(s"Consumer cancelled: $consumerTag")
      }

      val consumerTag = channel.basicConsume(ElasticIndexQueue.QUEUE_NAME, false, deliverCallback, cancelCallback)
      log.info(s"Elastic index consumer created with consumer tag: $consumerTag")

    } catch {
      case e: Exception =>
        log.error("Failed to start elastic index listener", e)
    }
  }

  private def logQueueCount(channel: Channel): Unit = {
    try {
      val countFromChannel = channel.messageCount(ElasticIndexQueue.QUEUE_NAME)
      log.info(s"Elastic index channel contains $countFromChannel ready to deliver messages")
    } catch {
      case e: Exception =>
        log.error("Error while counting messages: ", e)
    }
  }

}