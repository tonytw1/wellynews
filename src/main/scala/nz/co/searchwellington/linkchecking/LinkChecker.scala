package nz.co.searchwellington.linkchecking

import io.micrometer.core.instrument.MeterRegistry
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.http.RobotsAwareHttpFetcher
import nz.co.searchwellington.linkchecking.processors.LinkCheckerProcessor
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.apache.commons.validator.routines.UrlValidator
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID

import java.net.{URL, UnknownHostException}
import java.util.Date
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.util.Try

@Component class LinkChecker @Autowired()(mongoRepository: MongoRepository, contentUpdateService: ContentUpdateService,
                                          httpFetcher: RobotsAwareHttpFetcher,
                                          registry: MeterRegistry,
                                          processors: java.util.List[LinkCheckerProcessor]) extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[LinkChecker])
  private val CANT_CONNECT = -1

  private val urlValidator = new UrlValidator()

  private val checkedCounter = registry.counter("linkchecker_checked")
  private val failedCounter = registry.counter("linkchecker_failed")
  private val duplicateCounter = registry.counter("linkchecker_duplicate")

  {
    log.info("Autowired " + processors.asScala.size + " link checker processors: " + processors.asScala.map(_.getClass.getCanonicalName).mkString(", "))
  }

  // Given a resource id,
  // load the resource.
  // If it has a url fetch the url and process the loaded page
  // Update the last scanned timestamp
  def scanResource(resourceId: String, idempotencyValue: Option[Date])(implicit ec: ExecutionContext): Future[Boolean] = {
    log.info("Scanning resource: " + resourceId)
    val objectId = BSONObjectID.parse(resourceId).get

    // Load the resource and check it's idempotency value synchronously
    // Dispatch a future after the idempotency check
    val maybeResource = Await.result(mongoRepository.getResourceByObjectId(objectId), TenSeconds)

    maybeResource.map { resource =>
      val isNotDuplicateRequest = resource.last_scanned == idempotencyValue
      if (isNotDuplicateRequest) {
        log.info("Checking: " + resource.title + " (" + resource.page + ")")
        val now = Some(DateTime.now.toDate)
        log.info("Marking idempotency value for: " + resource.title + " as: " + now)
        resource.last_scanned = now
        Await.result(contentUpdateService.update(resource), TenSeconds)

        // Now that the idempotency value has been updated we can dispatch a future safe from duplicates
        checkResource(resource).map { outcome =>
          checkedCounter.increment()
          log.info("Finished link checking")
          outcome
        }

      } else {
        log.info("Skipping link check for " + resource.title + " as it has already been checked with idempotency: " + idempotencyValue + " / " + resource.last_scanned)
        duplicateCounter.increment()
        Future.successful(false)
      }

    }.getOrElse {
      log.warn("Link checker was past an unknown resource id: " + resourceId + " / " + objectId.stringify)
      failedCounter.increment()
      Future.successful(false)
    }

  }

  // Given a URL load it and return the http status and the page contents
  private def httpCheck(url: URL)(implicit ec: ExecutionContext): Future[Either[Int, (Integer, Boolean, Option[String])]] = {
    httpFetcher.httpFetch(url, followRedirects = false).flatMap { httpResult =>
      val status = httpResult.status
      log.info("Http status for " + url + " set was: " + status)

      val isRedirecting = status >= 300 && status < 400
      // val isMovedPermanently = status == 301 // TODO this is the useful signal we're really trying to capture here
      if (isRedirecting) {
        httpFetcher.httpFetch(url).map { httpResult =>
          log.info(s"Retrying fetching of $url with follow redirects")
          Right(httpResult.status, isRedirecting, Some(httpResult.body))
        }

      } else {
        Future.successful(Right(status, isRedirecting, Some(httpResult.body)))
      }

    }.recoverWith {
      case _: UnknownHostException =>
        log.warn(s"Link check http fetch failed with unknown host: $url")
        Future.successful(Left(-2))
      case e: Exception =>
        log.error("Link check http fetch failed: ", e)
        Future.successful(Left(CANT_CONNECT))
    }
  }

  def checkResource(resource: Resource)(implicit ec: ExecutionContext): Future[Boolean] = {
    val parsedUrl = {
      if (urlValidator.isValid(resource.page)) { // java.net.URL's construct is too permissive; ie. http://// // TODO Apply this everywhere
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
            resource.setHttpStatus(left, false)
            true
          }

        }, { case (status, redirecting, maybePageContent)  =>
          resource.setHttpStatus(status, redirecting)
          // Run each processor in turn letting them create their side effects and mutate the resource.
          // Then save the mutated resource.
          val eventualCheckedResource = processors.asScala.foldLeft(Future.successful(resource)) { (resourceFuture, processor) =>
            resourceFuture.flatMap { resource =>
              log.info("Running processor: " + processor.getClass.toString + " on resource: " + resource.title)
              processor.process(resource, maybePageContent, DateTime.now)
            }
          }
          eventualCheckedResource.flatMap { resource =>
            contentUpdateService.update(resource)
          }.map { _ =>
            true
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
