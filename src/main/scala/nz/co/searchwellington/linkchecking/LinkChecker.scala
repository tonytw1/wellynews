package nz.co.searchwellington.linkchecking

import io.micrometer.core.instrument.MeterRegistry
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.http.RobotsAwareHttpFetcher
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

  {
    log.info("Autowired " + processors.asScala.size + " link checker processors: " + processors.asScala.map(_.getClass.getCanonicalName).mkString(", "))
  }

  // Given a resource id,
  // load the resource.
  // If it has a url fetch the url and process the loaded page
  // Update the last scanned timestamp
  def scanResource(resourceId: String)(implicit ec: ExecutionContext): Unit = {
    log.info("Scanning resource: " + resourceId)
    val objectId = BSONObjectID.parse(resourceId).get

    val eventualResult = for {
      maybeResourceWithUrl <- mongoRepository.getResourceByObjectId(objectId)
      result <- maybeResourceWithUrl.map { resource =>
        log.info("Checking: " + resource.title + " (" + resource.page + ")")
        checkResource(resource).flatMap { outcome =>
          contentUpdateService.update(resource).map { _ =>
            checkedCounter.increment()
            log.info("Finished link checking")
            outcome
          }
        }

      }.getOrElse {
        log.warn("Link checker was past an unknown resource id: " + resourceId + " / " + objectId.stringify)
        failedCounter.increment()
        Future.successful(false)

      }.recoverWith {
        case e: Exception =>
          log.error("Link check failed: " + e.getMessage, e)
          failedCounter.increment()
          Future.successful(false)
      }

    } yield {
      result
    }

    Await.result(eventualResult, OneMinute)
  }

  // Given a URL load it and return the http status and the page contents
  private def httpCheck(url: URL)(implicit ec: ExecutionContext): Future[Either[Int, (Integer, Option[String])]] = {
    httpFetcher.httpFetch(url, followRedirects = false).flatMap { httpResult =>
      val status = httpResult.status
      log.info("Http status for " + url + " set was: " + status)

      val isRedirecting = status >= 300 && status < 400
      val isMovedPermanently = status == 301  // TODO this is the useful signal we're really trying to capture here
      if (isRedirecting) {
        httpFetcher.httpFetch(url).map { httpResult =>
          log.info(s"Retrying fetching of $url with follow redirects")
          Right(status, Some(httpResult.body))
        }

      } else {
        Future.successful(Right(status, Some(httpResult.body)))
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
      if (urlValidator.isValid(resource.page)) {  // java.net.URL's construct is too permissive; ie. http://// // TODO Apply this everywhere
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
          val eventualProcessorOutcomes = processors.asScala.map { processor =>
            log.debug("Running processor: " + processor.getClass.toString)
            processor.process(resource, right._2, DateTime.now)
          }

          Future.sequence(eventualProcessorOutcomes).map { processorOutcomes =>
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
