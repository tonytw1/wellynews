package nz.co.searchwellington.linkchecking

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

import scala.concurrent.{ExecutionContext, Future}

@Component class LinkChecker @Autowired()(mongoRepository: MongoRepository, contentUpdateService: ContentUpdateService,
                                          httpFetcher: RobotsAwareHttpFetcher, feedAutodiscoveryProcesser: FeedAutodiscoveryProcesser,
                                          feedReaderTaskExecutor: TaskExecutor) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[LinkChecker])
  private val CANT_CONNECT = -1

  implicit val executionContext = ExecutionContext.fromExecutor(feedReaderTaskExecutor)
  private val processers: Seq[LinkCheckerProcessor] = Seq(feedAutodiscoveryProcesser) // TODO inject all

  //val snapshotArchive = new FilesystemSnapshotArchive("/home/tony/snapshots")

  def scanResource(checkResourceId: String) {
    log.info("Scanning resource: " + checkResourceId)

    mongoRepository.getResourceByObjectId(BSONObjectID(checkResourceId)).flatMap { maybeResource =>
      val mayToCheck = maybeResource.flatMap { resource =>
        resource.page.flatMap { page =>
          if (page.nonEmpty) {
            Some(resource, page)
          } else {
            None
          }
        }
      }

      mayToCheck.map { toCheck =>
        log.info("Checking: " + toCheck._1.title + " (" + toCheck + ")")
        httpCheck(toCheck._1, toCheck._2).map { maybePageBody =>
          maybePageBody.map { pageBody =>
            processers.foreach { processor =>
              log.debug("Running processor: " + processor.getClass.toString)
              try {
                processor.process(toCheck._1, pageBody, DateTime.now)
              } catch {
                case e: Exception =>
                  log.error("An exception occured while running a link checker processor", e)
              }
              //snapshotArchive.put(new Snapshot(p, DateTime.now.toDate, pageContent))
            }
            true
          }

          log.debug("Saving resource and updating snapshot")
          toCheck._1.setLastScanned(DateTime.now.toDate)

          contentUpdateService.update(toCheck._1) // TODO should be a specific field set
          log.info("Finished linkchecking")
          true
        }

      }.getOrElse {
        Future.successful(false)
      }
    }
  }

  private def httpCheck(checkResource: Resource, url: String): Future[Option[String]] = {
    httpFetcher.httpFetch(url).map { httpResult =>
      try {
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

}
