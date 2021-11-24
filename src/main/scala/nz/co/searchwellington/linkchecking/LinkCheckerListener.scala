package nz.co.searchwellington.linkchecking

import com.rabbitmq.client.{AMQP, Channel, DefaultConsumer, Envelope}
import io.micrometer.core.instrument.MeterRegistry
import nz.co.searchwellington.queues.{LinkCheckerQueue, RabbitConnectionFactory}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

@Component class LinkCheckerListener @Autowired()(linkChecker: LinkChecker, rabbitConnectionFactory: RabbitConnectionFactory,
                                                  @Qualifier("linkCheckerTaskExecutor") linkCheckerTaskExecutor: TaskExecutor,
                                                  registry: MeterRegistry) {

  private val log = Logger.getLogger(classOf[LinkCheckerListener])

  private val pulledCounter = registry.counter("linkchecker_pulled")

  {
    log.info("Starting link check listener")
    try {
      val connection = rabbitConnectionFactory.connect
      val channel = connection.createChannel
      channel.queueDeclare(LinkCheckerQueue.QUEUE_NAME, false, false, false, null)
      val consumer = new LinkCheckerConsumer(channel)
      val consumerTag = channel.basicConsume(LinkCheckerQueue.QUEUE_NAME, false, consumer)
      log.info(s"Link checker consumer thread started with consumer tag: $consumerTag")

    } catch {
      case e: Exception =>
        log.error(e)
    }
  }

  class LinkCheckerConsumer(channel: Channel) extends DefaultConsumer(channel: Channel) {
    private implicit val executionContext: ExecutionContextExecutor = ExecutionContext.fromExecutor(linkCheckerTaskExecutor)

    override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
      try {
        log.debug(s"Link checker handling delivery with consumer tag: $consumerTag")
        val message = new String(body)
        log.debug("Received link checker message: " + message)
        pulledCounter.increment()
        linkChecker.scanResource(message)
      } catch {
        case e: Exception =>
          log.error("Error while processing link checker message: ", e)
      }
    }
  }

}