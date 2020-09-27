package nz.co.searchwellington.linkchecking

import java.io.IOException

import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.queues.RabbitConnectionFactory
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import com.rabbitmq.client.Channel
import com.rabbitmq.client.QueueingConsumer
import org.springframework.core.task.TaskExecutor

import scala.concurrent.ExecutionContext

@Component class LinkCheckerListener @Autowired() (linkChecker: LinkChecker, rabbitConnectionFactory: RabbitConnectionFactory,
                                                   linkCheckerTaskExecutor: TaskExecutor) {

  private val log = Logger.getLogger(classOf[LinkCheckerListener])

  val QUEUE_NAME = LinkCheckerQueue.QUEUE_NAME

  implicit val executionContext = ExecutionContext.fromExecutor(linkCheckerTaskExecutor)

  {
    log.info("Starting link check listener")
    val connection = rabbitConnectionFactory.connect
    val channel = connection.createChannel
    channel.queueDeclare (QUEUE_NAME, false, false, false, null)
    val consumerThread = new Thread (new ConsumerThread(channel, linkChecker))
    consumerThread.start()
  }

  private[linkchecking] class ConsumerThread(channel: Channel, linkChecker: LinkChecker) extends Runnable {

    private val QUEUE_NAME = LinkCheckerQueue.QUEUE_NAME
    private val log = Logger.getLogger(classOf[LinkCheckerListener])

    override def run(): Unit = {
      val consumer = new QueueingConsumer(channel)
      try channel.basicConsume(QUEUE_NAME, true, consumer)
      catch {
        case e: IOException =>
          log.error(e)
      }
      while ( {
        true
      }) try {
        val delivery = consumer.nextDelivery
        val message = new String(delivery.getBody)
        log.info("Received: " + message)
        linkChecker.scanResource(message)
      } catch {
        case e: Exception =>
          log.error(e)
      }
    }

  }

}