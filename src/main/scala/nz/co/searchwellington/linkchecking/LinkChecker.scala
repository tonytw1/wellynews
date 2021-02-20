package nz.co.searchwellington.linkchecking

import java.net.{URL, UnknownHostException}

import io.micrometer.core.instrument.MeterRegistry
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.http.RobotsAwareHttpFetcher
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.{Await, ExecutionContext, Future}

@Component class LinkChecker @Autowired()(mongoRepository: MongoRepository, contentUpdateService: ContentUpdateService,
                                          httpFetcher: RobotsAwareHttpFetcher, feedAutodiscoveryProcesser: FeedAutodiscoveryProcesser,
                                          registry: MeterRegistry)
  extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[LinkChecker])
  private val CANT_CONNECT = -1

  private val processers: Seq[LinkCheckerProcessor] = Seq(feedAutodiscoveryProcesser) // TODO inject all

  private val checkedCounter = registry.counter("linkchecker_checked")

  //val snapshotArchive = new FilesystemSnapshotArchive("/home/tony/snapshots")


  // Given a resource id,
  // load the resource.
  // If it has a url fetch the url and process the loaded page
  // Update the last scanned timestamp
  def scanResource(resourceId: String)(implicit ec: ExecutionContext) {
    log.info("Scanning resource: " + resourceId)

    val eventualMaybeResource= mongoRepository.getResourceByObjectId(BSONObjectID.parse(resourceId).get)
    val eventualMaybeResourceWithUrl = eventualMaybeResource.map { mayByResource =>
      mayByResource.filter(_.page.nonEmpty)
    }

    val y: Future[Boolean] = eventualMaybeResourceWithUrl.flatMap { maybeResourceWithUrl =>
      val a = maybeResourceWithUrl.map { resource =>
        log.info("Checking: " + resource.title + " (" + resource.page + ")")

        val url = new java.net.URL(resource.page) // TODO This parsing can fail hard

        val eventualHttpCheckOutcome = httpCheck(url).flatMap { result =>
          result.fold({ left =>
            Future.successful {
              resource.setHttpStatus(left)
              true
            }

          }, { right =>
            resource.setHttpStatus(right._1)

            val eventualProcesserOutcomes = processers.map { processor =>
              log.debug("Running processor: " + processor.getClass.toString)
              processor.process(resource, right._2, DateTime.now)
            }

            Future.sequence(eventualProcesserOutcomes).map { processorOutcomes =>
              processorOutcomes.forall(outcome => outcome)
            }
          })
        }

        val z: Future[Boolean] = eventualHttpCheckOutcome.flatMap { outcome =>
          log.debug("Updating resource")  // TODO push upwards to capture fail as well
          resource.setLastScanned(DateTime.now.toDate)
          contentUpdateService.update(resource).map { _ => // TODO should be a specific field set
            checkedCounter.increment()
            log.info("Finished link checking")
            outcome
          }
        }
        z

      }.getOrElse {
        log.warn("Link checker was past an unknown resource id: " + resourceId)
        Future.successful(false)
      }
      a

    }.recoverWith{
      case e: Exception =>
        log.error("Link check failed: ", e)
        Future.successful(false)
    }

    Await.result(y, OneMinute)
  }

  // Given a URL load it and return the http status and the page contents
  private def httpCheck(url: URL)(implicit ec: ExecutionContext): Future[Either[Int, (Integer, String)]] = {
    httpFetcher.httpFetch(url).map { httpResult =>
      log.info("Http status for " + url + " set was: " + httpResult.status)
      Right(httpResult.status, httpResult.body)
    }.recoverWith {
      case e: UnknownHostException =>
        log.error("Link check http fetch failed: ", e)
        Future.successful(Left(-2))
      case e: Exception =>
        log.error("Link check http fetch failed: ", e)
        Future.successful(Left(CANT_CONNECT))
    }
  }

}
