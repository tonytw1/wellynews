package nz.co.searchwellington.linkchecking

import com.rabbitmq.client.{CancelCallback, Channel, DeliverCallback, Delivery}
import io.micrometer.core.instrument.MeterRegistry
import nz.co.searchwellington.queues.{LinkCheckerQueue, RabbitConnectionFactory, RestrainedRabbitConnection}
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Component
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

@Component class LinkCheckerConsumer @Autowired()(linkChecker: LinkChecker,
                                                  val rabbitConnectionFactory: RabbitConnectionFactory,
                                                  @Qualifier("linkCheckerTaskExecutor") linkCheckerTaskExecutor: TaskExecutor,
                                                  registry: MeterRegistry) extends RestrainedRabbitConnection {

  private val log = LogFactory.getLog(classOf[LinkCheckerConsumer])

  private val pulledCounter = registry.counter("linkchecker_pulled")

  private val maximumConcurrentChecks = 10

  {
    log.info("Starting link check listener")
    try {
      val channel = channelWithMaximumConcurrentChecks(maximumConcurrentChecks)

      implicit val executionContext: ExecutionContextExecutor = ExecutionContext.fromExecutor(linkCheckerTaskExecutor)

      val deliverCallback: DeliverCallback = (consumerTag: String, message: Delivery) => {
        try {
          log.debug(s"Link checker handling delivery with consumer tag: $consumerTag")
          pulledCounter.increment()
          val body = message.getBody
          val asJson = new String(body)
          val request = Json.parse(asJson).as[LinkCheckRequest]

          linkChecker.scanResource(request.resourceId, request.lastScanned).map { _ =>
            channel.basicAck(message.getEnvelope.getDeliveryTag, false)
            logQueueCount(channel)
          }

        }
        catch {
          case e: Throwable =>
            log.error("Error while processing; rejecting message ", e)
            channel.basicReject(message.getEnvelope.getDeliveryTag, false)
        }
      }

      val cancelCallback: CancelCallback = (consumerTag: String) => {
        log.info(s"Consumer cancelled: $consumerTag")
      }

      val consumerTag = channel.basicConsume(LinkCheckerQueue.QUEUE_NAME, false, deliverCallback, cancelCallback)
      log.info(s"Link checker consumer created with consumer tag: $consumerTag")

    } catch {
      case e: Throwable =>
        log.error("Link checker listener exception: ", e)
    }
  }

  private def logQueueCount(channel: Channel): Unit = {
    try {
      val countFromChannel = channel.messageCount(LinkCheckerQueue.QUEUE_NAME)
      log.info(s"Link checker channel contains $countFromChannel ready to deliver messages")
    } catch {
      case e: Exception =>
        log.error("Error while counting messages: ", e)
    }
  }

}