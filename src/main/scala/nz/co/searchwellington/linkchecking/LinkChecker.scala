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
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.{Await, ExecutionContext, Future}

@Component class LinkChecker @Autowired()(mongoRepository: MongoRepository, contentUpdateService: ContentUpdateService,
                                          httpFetcher: RobotsAwareHttpFetcher, feedAutodiscoveryProcesser: FeedAutodiscoveryProcesser)
  extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[LinkChecker])
  private val CANT_CONNECT = -1

  private val processers: Seq[LinkCheckerProcessor] = Seq(feedAutodiscoveryProcesser) // TODO inject all

  //val snapshotArchive = new FilesystemSnapshotArchive("/home/tony/snapshots")

  def scanResource(checkResourceId: String)(implicit ec: ExecutionContext) {
    log.info("Scanning resource: " + checkResourceId)

    mongoRepository.getResourceByObjectId(BSONObjectID.parse(checkResourceId).get).flatMap { maybeResource =>
      val mayToCheck = maybeResource.flatMap { resource =>
        val page = resource.page
        if (page.nonEmpty) {
          Some(resource, page)
        } else {
          None
        }
      }

      mayToCheck.map { toCheck =>
        log.info("Checking: " + toCheck._1.title + " (" + toCheck._1.page + ")")
        httpCheck(toCheck._1, toCheck._2).map { maybePageBody =>
          maybePageBody.map { pageBody =>
            val x: Seq[Future[Boolean]] = processers.map { processor =>
              log.debug("Running processor: " + processor.getClass.toString)
              processor.process(toCheck._1, pageBody, DateTime.now)
              //snapshotArchive.put(new Snapshot(p, DateTime.now.toDate, pageContent))
            }

            Await.result(Future.sequence(x), TenSeconds)
            true
          }

          log.debug("Saving resource and updating snapshot")
          toCheck._1.setLastScanned(DateTime.now.toDate)

          contentUpdateService.update(toCheck._1) // TODO should be a specific field set
          log.info("Finished link checking")
          true
        }

      }.getOrElse {
        Future.successful(false)
      }
    }
  }

  private def httpCheck(checkResource: Resource, url: String)(implicit ec: ExecutionContext): Future[Option[String]] = {
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
