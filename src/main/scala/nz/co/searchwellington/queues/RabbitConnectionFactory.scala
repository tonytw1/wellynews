package nz.co.searchwellington.queues

import com.rabbitmq.client.{Connection, ConnectionFactory}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component

@Component
class RabbitConnectionFactory @Autowired()(@Value("${rabbit.hostname}") val rabbitHost: String,
                                           @Value("${rabbit.port}") val rabbitPort: Integer) {

  private val factory = {
    val factory = new ConnectionFactory
    factory.setHost(rabbitHost)
    factory.setPort(rabbitPort)
    factory
  }

  def connect: Connection = factory.newConnection

}