package nz.co.searchwellington.queues

import com.rabbitmq.client.Channel

trait RestrainedRabbitConnection {

  def rabbitConnectionFactory: RabbitConnectionFactory

  def channelWithMaximumConcurrentChecks(maximumConcurrentChecks: Int): Channel = {
    val connection = rabbitConnectionFactory.connect

    val channel = connection.createChannel
    // Our consumers immediately dispatch each new message into a Future.
    // There is no back pressure from the consumer to stop Rabbit flooding us.
    // So we'll use the Rabbit channel maximum unacked messages / Qos as our flow control.
    channel.basicQos(maximumConcurrentChecks)
    channel
  }

}
