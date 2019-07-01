package nz.co.searchwellington.linkchecking

import com.google.common.base.Strings
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.http.RobotsAwareHttpFetcher
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.http.HttpStatus
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Component
import reactivemongo.bson.BSONObjectID

import scala.concurrent.{Await, ExecutionContext}

@Component class LinkChecker @Autowired() (mongoRepository: MongoRepository, contentUpdateService: ContentUpdateService,
                                           httpFetcher: RobotsAwareHttpFetcher, feedAutodiscoveryProcesser: FeedAutodiscoveryProcesser, feedReaderTaskExecutor: TaskExecutor)
extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[LinkChecker])
  private val CANT_CONNECT = -1

  implicit val executionContext = ExecutionContext.fromExecutor(feedReaderTaskExecutor)

  //val snapshotArchive = new FilesystemSnapshotArchive("/home/tony/snapshots")

  def scanResource(checkResourceId: String) {
    log.info("Scanning resource: " + checkResourceId)

    val processers: Seq[LinkCheckerProcessor] = Seq(feedAutodiscoveryProcesser) // TODO inject all

    Await.result(mongoRepository.getResourceByObjectId(BSONObjectID(checkResourceId)).map { maybeResource =>
      maybeResource.map { resource =>
        resource.page.map { p =>
          if (!Strings.isNullOrEmpty(p)) {

            log.info("Checking: " + resource.title + " (" + p + ")")
            httpCheck(resource, p).map { pageBody =>
              for (processor <- processers) {
                log.debug("Running processor: " + processor.getClass.toString)
                try {
                  processor.process(resource, pageBody, DateTime.now)
                } catch {
                  case e: Exception =>
                    log.error("An exception occured while running a link checker processor", e)
                }
                //snapshotArchive.put(new Snapshot(p, DateTime.now.toDate, pageContent))
              }
            }

            log.debug("Saving resource and updating snapshot")
            resource.setLastScanned(DateTime.now.toDate)

            contentUpdateService.update(resource) // TODO should be a specific field set
            log.info("Finished linkchecking")
            true

          } else {
            log.warn("Empty url for: " + resource.id)
            false
          }
        }.getOrElse {
          true
        }
      }

    }, OneMinute)
  }

  private def httpCheck(checkResource: Resource, url: String): Option[String] = {
    try {
      val httpResult = httpFetcher.httpFetch(url)
      checkResource.setHttpStatus(httpResult.status)
      log.info("Http status for " + checkResource.page + " set to: " + checkResource.http_status)

      if (httpResult.status == HttpStatus.SC_OK) {
        Some(httpResult.body)
      } else {
        checkResource.setHttpStatus(httpResult.status)
        None

      }
    }
    catch {
      case e: Exception =>
        log.error("Error while checking url: ", e)
        checkResource.setHttpStatus(CANT_CONNECT)
        None
    }
  }

}
