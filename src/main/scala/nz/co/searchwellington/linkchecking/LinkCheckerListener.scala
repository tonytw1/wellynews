package nz.co.searchwellington.linkchecking

import com.rabbitmq.client.{Channel, QueueingConsumer}
import io.micrometer.core.instrument.MeterRegistry
import nz.co.searchwellington.queues.{LinkCheckerQueue, RabbitConnectionFactory}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext

@Component class LinkCheckerListener @Autowired() (linkChecker: LinkChecker, rabbitConnectionFactory: RabbitConnectionFactory,
                                                   linkCheckerTaskExecutor: TaskExecutor,
                                                   registry: MeterRegistry) {

  private val log = Logger.getLogger(classOf[LinkCheckerListener])

  val QUEUE_NAME = LinkCheckerQueue.QUEUE_NAME

  private val pulledCounter = registry.counter("linkchecker_pulled")

  implicit val executionContext = ExecutionContext.fromExecutor(linkCheckerTaskExecutor)

  {
    log.info("Starting link check listener")
    try {
      val connection = rabbitConnectionFactory.connect
      val channel = connection.createChannel
      channel.queueDeclare(QUEUE_NAME, false, false, false, null)
      val consumerThread = new Thread(new ConsumerThread(channel, linkChecker))
      consumerThread.start()
      log.info("Link checker consumer thread started")

    } catch {
      case e: Exception =>
        log.error(e)
    }
  }

  private[linkchecking] class ConsumerThread(channel: Channel, linkChecker: LinkChecker) extends Runnable {

    private val QUEUE_NAME = LinkCheckerQueue.QUEUE_NAME
    private val log = Logger.getLogger(classOf[LinkCheckerListener])

    override def run(): Unit = {
      log.info("Link checker consumer thread running")

      val consumer = new QueueingConsumer(channel)
      try channel.basicConsume(QUEUE_NAME, true, consumer)
      catch {
        case e: Exception =>
          log.error(e)
      }


      while ( {
        true

      }) try {
        log.info("Link checker consumer awaiting delivery ")
        val delivery = consumer.nextDelivery
        val message = new String(delivery.getBody)
        log.info("Received: " + message)
        pulledCounter.increment()
        linkChecker.scanResource(message)

      } catch {
        case e: Exception =>
          log.error("Error while processing link checker message: ", e)
      }
    }
  }

}