package nz.co.searchwellington.linkchecking

import java.net.{URL, UnknownHostException}
import io.micrometer.core.instrument.MeterRegistry
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.http.RobotsAwareHttpFetcher
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.validator.UrlValidator
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

@Component class LinkChecker @Autowired()(mongoRepository: MongoRepository, contentUpdateService: ContentUpdateService,
                                          httpFetcher: RobotsAwareHttpFetcher,
                                          feedAutodiscoveryProcessor: FeedAutodiscoveryProcesser,
                                          twitterPhotoDetector: TwitterPhotoDetector,
                                          registry: MeterRegistry)
  extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[LinkChecker])
  private val CANT_CONNECT = -1

  private val processors = Seq(feedAutodiscoveryProcessor, twitterPhotoDetector) // TODO inject all

  private val checkedCounter = registry.counter("linkchecker_checked")
  private val failedCounter = registry.counter("linkchecker_failed")

  //val snapshotArchive = new FilesystemSnapshotArchive("/home/tony/snapshots")


  // Given a resource id,
  // load the resource.
  // If it has a url fetch the url and process the loaded page
  // Update the last scanned timestamp
  def scanResource(resourceId: String)(implicit ec: ExecutionContext) {
    log.info("Scanning resource: " + resourceId)
    val objectId = BSONObjectID.parse(resourceId).get
    val eventualMaybeResourceWithUrl = mongoRepository.getResourceByObjectId(objectId).map { mayByResource =>
      mayByResource.filter(_.page.nonEmpty)
    }

    val y: Future[Boolean] = eventualMaybeResourceWithUrl.flatMap { maybeResourceWithUrl =>
      val a = maybeResourceWithUrl.map { resource =>
        log.info("Checking: " + resource.title + " (" + resource.page + ")")

        val j = checkResource(resource)

        val g = j.flatMap { outcome =>
          log.debug("Updating resource")
          resource.setLastScanned(DateTime.now.toDate)
          contentUpdateService.update(resource).map { _ => // TODO should be a specific field set
            checkedCounter.increment()
            log.info("Finished link checking")
            outcome
          }
        }
        g

      }.getOrElse {
        log.warn("Link checker was past an unknown resource id: " + resourceId + " / " + objectId.stringify)
        failedCounter.increment()
        Future.successful(false)
      }
      a

    }.recoverWith {
      case e: Exception =>
        log.error("Link check failed: " + e.getMessage, e)
        failedCounter.increment()
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

  def checkResource(resource: Resource)(implicit ec: ExecutionContext): Future[Boolean] = {
    val parsedUrl = {
      if (new UrlValidator().isValid(resource.page)) {  // java.net.URL's construct is too permissive; ie. http:////
        Try {
          new java.net.URL(resource.page)
        }.toOption
      } else {
        None
      }
    }

    parsedUrl.map { url =>
      val eventualHttpCheckOutcome = httpCheck(url).flatMap { result =>
        result.fold({ left =>
          Future.successful {
            resource.setHttpStatus(left)
            true
          }

        }, { right =>
          resource.setHttpStatus(right._1)

          val eventualProcesserOutcomes = processors.map { processor =>
            log.debug("Running processor: " + processor.getClass.toString)
            processor.process(resource, right._2, DateTime.now)
          }

          Future.sequence(eventualProcesserOutcomes).map { processorOutcomes =>
            processorOutcomes.forall(outcome => outcome)
          }
        })
      }
      eventualHttpCheckOutcome

    }.getOrElse {
      log.warn("Resource had an unparsable url: " + resource.page)
      Future.successful(false)
    }

  }

}
