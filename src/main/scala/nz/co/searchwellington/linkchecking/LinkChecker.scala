package nz.co.searchwellington.linkchecking

import com.google.common.base.Strings
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.HibernateResourceDAO
import nz.co.searchwellington.utils.HttpFetcher
import org.apache.commons.httpclient.HttpStatus
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.archiving.{FilesystemSnapshotArchive, Snapshot}

@Component class LinkChecker @Autowired() (resourceDAO: HibernateResourceDAO, contentUpdateService: ContentUpdateService, httpFetcher: HttpFetcher) {

  private val log = Logger.getLogger(classOf[LinkChecker])
  private val CANT_CONNECT = -1

  val snapshotArchive = new FilesystemSnapshotArchive("/home/tony/snapshots")

  def scanResource(checkResourceId: Int) {

    val processers: Seq[LinkCheckerProcessor] = Seq() // TODO inject

    resourceDAO.loadResourceById(checkResourceId).map { resource =>
      if (resource != null) {
        resource.page.map { p =>
          if (!Strings.isNullOrEmpty(p)) {
            log.info("Checking: " + resource.title + " (" + p + ")")
            val pageContent = httpCheck(resource, p)

            for (processor <- processers) {
              log.debug("Running processor: " + processor.getClass.toString)
              try {
                processor.process(resource, pageContent)
              }
              catch {
                case e: Exception =>
                  log.error("An exception occured while running a link checker processor", e)
              }
            }

            log.debug("Saving resource and updating snapshot")
            resource.setLastScanned(DateTime.now.toDate)
            if (pageContent != null) {
              snapshotArchive.put(new Snapshot(p, DateTime.now.toDate, pageContent))
            }
            contentUpdateService.update(resource)
            log.info("Finished linkchecking")
          }
        }

      } else {
        log.warn("Could not check resource with id #" + checkResourceId + " as it was not found in the database")
      }
    }
  }

  private def httpCheck(checkResource: Resource, url: String): String = {
    try {
      val httpResult = httpFetcher.httpFetch(url)
      checkResource.setHttpStatus(httpResult.getStatus)
      log.info("Http status for " + checkResource.page + " set to: " + checkResource.http_status)
      if (httpResult.getStatus == HttpStatus.SC_OK) {
        httpResult.readEncodedResponse("UTF-8")
      } else {
        checkResource.setHttpStatus(httpResult.getStatus)
        null
      }
    }
    catch {
      case e: Exception =>
        log.error("Error while checking url: ", e)
        checkResource.setHttpStatus(CANT_CONNECT)
        null
    }
  }

}
