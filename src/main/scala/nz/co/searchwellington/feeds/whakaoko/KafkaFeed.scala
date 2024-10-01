package nz.co.searchwellington.feeds.whakaoko

import org.apache.commons.logging.LogFactory
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.springframework.stereotype.Component

import java.time.Duration
import java.util.{Collections, Properties}
import scala.jdk.CollectionConverters.IterableHasAsScala

@Component
class KafkaFeed {

  private val log = LogFactory.getLog(classOf[KafkaFeed])

  val config = new Properties();
  config.put("client.id", java.util.UUID.randomUUID().toString)
  config.put("group.id", "wellynews")
  config.put("bootstrap.servers", "10.0.46.10:32192")
  config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
  config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
  config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  // TODO blocks start up!
  /*
  private val consumer: KafkaConsumer[String, String] = new KafkaConsumer(config)
  log.info("Subscribing")
  consumer.subscribe(Collections.singletonList("test"))
  while (true) {
    log.info("Polling")
    val records = consumer.poll(Duration.ofSeconds(10)).asScala
    records.foreach { r =>
      log.info(s"Got value from ${r.topic}: " + r.value())
    }
  }
 */

}
