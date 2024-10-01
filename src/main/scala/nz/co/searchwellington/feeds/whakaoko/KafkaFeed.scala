package nz.co.searchwellington.feeds.whakaoko

import cats.effect.IO
import fs2.kafka._
import org.apache.commons.logging.LogFactory
import org.springframework.stereotype.Component

@Component
class KafkaFeed {

  private val log = LogFactory.getLog(classOf[KafkaFeed])

  private val consumerSettings =
    ConsumerSettings[IO, String, String]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("10.0.46.10:32192")
      .withGroupId("wellynews")

  def processRecord(record: ConsumerRecord[String, String]): IO[Unit] = {
    IO(log.info(s"Processing Kafka record: $record"))
  }

  val run: IO[Unit] = {
    log.info("Building stream")
    val stream =
      KafkaConsumer
        .stream(consumerSettings)
        .subscribeTo("whakaoko.wellynews")
        .partitionedRecords
        .map { partitionStream =>
          partitionStream.evalMap { committable =>
            processRecord(committable.record)
          }

        }.parJoinUnbounded

    stream.compile.drain
  }

}
