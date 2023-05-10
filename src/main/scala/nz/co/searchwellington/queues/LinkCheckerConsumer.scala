package nz.co.searchwellington.queues

import com.rabbitmq.client.{AMQP, Channel, DefaultConsumer, Envelope}
import io.micrometer.core.instrument.MeterRegistry
import nz.co.searchwellington.linkchecking.LinkChecker
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Component

import scala.compat.java8.functionConverterImpls.AsJavaToDoubleFunction
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

@Component class LinkCheckerConsumer @Autowired()(linkChecker: LinkChecker, rabbitConnectionFactory: RabbitConnectionFactory,
                                                  @Qualifier("linkCheckerTaskExecutor") linkCheckerTaskExecutor: TaskExecutor,
                                                  registry: MeterRegistry) {

  private val log = LogFactory.getLog(classOf[LinkCheckerConsumer])

  private val pulledCounter = registry.counter("linkchecker_pulled")

  {
    log.info("Starting link check listener")
    try {
      val connection = rabbitConnectionFactory.connect
      val channel = connection.createChannel
      channel.basicQos(1)

      val ok = channel.queueDeclare(LinkCheckerQueue.QUEUE_NAME, false, false, false, null)
      log.info(s"Link checker queue declared; contains ${ok.getMessageCount} messages")

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

    private def countMessages(channel: Channel): Double = {
      channel.messageCount(LinkCheckerQueue.QUEUE_NAME)
    }
    val countMessagesToDoubleFunction: AsJavaToDoubleFunction[Channel] = new AsJavaToDoubleFunction(countMessages)

    registry.gauge("linkchecker_queue", channel, countMessagesToDoubleFunction)

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

      channel.basicAck(envelope.getDeliveryTag, false)
      logQueueCount(channel)
    }
  }

  private def logQueueCount(channel: Channel): Unit = {
    try {
      val ok = channel.queueDeclare(LinkCheckerQueue.QUEUE_NAME, false, false, false, null)
      val countFromDeclare = ok.getMessageCount
      val countFromChannel = channel.messageCount(LinkCheckerQueue.QUEUE_NAME)

      log.info(s"Link checker queue contains $countFromDeclare / $countFromChannel messages")
    } catch {
      case e: Exception =>
        log.error("Error while counting messages: ", e)
    }
  }

}