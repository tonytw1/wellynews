package nz.co.searchwellington.feeds.reading

import com.rabbitmq.client.{CancelCallback, DeliverCallback, Delivery}
import io.micrometer.core.instrument.MeterRegistry
import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.feeds.FeedReader
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, User}
import nz.co.searchwellington.queues.{RabbitConnectionFactory, ReadFeedQueue}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Component
import play.api.libs.json.Json
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

@Component class ReadFeedConsumer @Autowired()(rabbitConnectionFactory: RabbitConnectionFactory,
                                               @Qualifier("feedReaderTaskExecutor") feedReaderTaskExecutor: TaskExecutor,
                                               feedReader: FeedReader,
                                               mongoRepository: MongoRepository,
                                               registry: MeterRegistry) {

  private val log = LogFactory.getLog(classOf[ReadFeedConsumer])

  private val pulledCounter = registry.counter("readfeed_pulled")

  private val maximumConcurrentChecks = 3

  {
    log.info("Starting read feed listener")
    try {
      val connection = rabbitConnectionFactory.connect

      val channel = connection.createChannel
      // The consumer immediately dispatches each new message into a Future.
      // There is no back pressure from the consumer to stop Rabbit flooding us.
      // So we'll use the Rabbit channel maximum unacked messages / Qos as our flow control.
      channel.basicQos(maximumConcurrentChecks)

      implicit val executionContext: ExecutionContextExecutor = ExecutionContext.fromExecutor(feedReaderTaskExecutor)

      val deliverCallback: DeliverCallback = (consumerTag: String, message: Delivery) => {
        implicit val currentSpan: Span = Span.current()

        try {
          log.debug(s"Read feed handling delivery with consumer tag: $consumerTag")
          pulledCounter.increment()
          val body = message.getBody
          val asJson = new String(body)
          val request = Json.parse(asJson).as[ReadFeedRequest]

          val maybeSpecificAcceptancePolicy = request.acceptedPolicy.map(FeedAcceptancePolicy.valueOf)

          val eventualMaybeFeedReaderUser = mongoRepository.getUserByObjectId(BSONObjectID.parse(request.asUserId).get)
          val eventualMaybeFeed = mongoRepository.getResourceByObjectId(BSONObjectID.parse(request.feedId).get).map {
            case f: Some[Feed] =>
              f
            case _ => None
          }

          val eventualEventualMaybeTuple: Future[Option[(Feed, User)]] = eventualMaybeFeedReaderUser.flatMap { maybeFeedReaderUser =>
            eventualMaybeFeed.map { maybeFeed =>
              maybeFeedReaderUser.flatMap { feedReaderUser =>
                maybeFeed.map { feed =>
                  (feed, feedReaderUser)
                }
              }
            }
          }

          val eventualAcceptedCount = eventualEventualMaybeTuple.flatMap { maybeTuple =>
            maybeTuple.map { case (feed, feedReaderUser) =>
              feedReader.processFeed(feed, feedReaderUser, maybeSpecificAcceptancePolicy)

            }.getOrElse {
              log.warn("Failed to find feed or feed reader user")
              Future.successful(0)
            }
          }


          eventualAcceptedCount.map { accepted =>
            log.info(s"Read feed accepted $accepted items")
            channel.basicAck(message.getEnvelope.getDeliveryTag, false)

          }.recover {
            case e: Exception =>
              log.error("Failed to read feed", e)
          }

        }
        catch {
          case e: Exception =>
            log.error(s"Failed to handle read feed message", e)
        }
      }

      val cancelCallback: CancelCallback = (consumerTag: String) => {
        log.info(s"Consumer cancelled: $consumerTag")
      }

      val consumerTag = channel.basicConsume(ReadFeedQueue.QUEUE_NAME, false, deliverCallback, cancelCallback)
      log.info(s"Read feed consumer created with consumer tag: $consumerTag")

    } catch {
      case e: Exception =>
        log.error(e)
    }
  }

}