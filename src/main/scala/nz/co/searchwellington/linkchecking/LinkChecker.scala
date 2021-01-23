package nz.co.searchwellington.linkchecking

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
  def scanResource(checkResourceId: String)(implicit ec: ExecutionContext) {
    log.info("Scanning resource: " + checkResourceId)

    val eventualMaybeResource: Future[Option[Resource]] = mongoRepository.getResourceByObjectId(BSONObjectID.parse(checkResourceId).get)
    val eventualMaybeResourceWithUrl = eventualMaybeResource.map { mayByResource =>
      mayByResource.filter(_.page.nonEmpty)
    }

    eventualMaybeResourceWithUrl.map { maybeResourceWithUrl =>
      maybeResourceWithUrl.map { resource =>
        log.info("Checking: " + resource.title + " (" + resource.page + ")")

        val x = httpCheck(resource.page).flatMap { result =>
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

            Future.sequence(eventualProcesserOutcomes).map { _ =>
              true
            }
          })
        }.recoverWith{
          case e: Exception =>
            log.error("Link check http fetch failed: ", e)
            Future.successful(false)
        }

        val z = x.flatMap { _ =>
          log.debug("Updating resource")
          resource.setLastScanned(DateTime.now.toDate)
          contentUpdateService.update(resource).map { _ => // TODO should be a specific field set
            checkedCounter.increment()
            log.info("Finished link checking")
          }
        }

        Await.result(z, OneMinute)
      }
    }

  }

  // Given a URL load it and return the http status and the page contents
  def httpCheck(url: String)(implicit ec: ExecutionContext): Future[Either[Int, (Integer, String)]] = {
    httpFetcher.httpFetch(url).map { httpResult =>
      log.info("Http status for " + url + " set was: " + httpResult.status)
      Right(httpResult.status, httpResult.body)
    }.recoverWith {
      case e: Exception =>
        log.error("Link check http fetch failed: ", e)
        Future.successful(Left(CANT_CONNECT))
    }
  }

}
