package nz.co.searchwellington.linkchecking

import java.io.IOException
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.HibernateResourceDAO
import nz.co.searchwellington.utils.HttpFetchResult
import nz.co.searchwellington.utils.HttpFetcher
import org.apache.commons.httpclient.HttpStatus
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import com.google.common.base.Strings
import uk.co.eelpieconsulting.archiving.FilesystemSnapshotArchive
import uk.co.eelpieconsulting.archiving.Snapshot
import uk.co.eelpieconsulting.archiving.SnapshotArchive

@Component class LinkChecker @Autowired() (resourceDAO: HibernateResourceDAO, contentUpdateService: ContentUpdateService, httpFetcher: HttpFetcher) {

  private val log = Logger.getLogger(classOf[LinkChecker])
  private val CANT_CONNECT: Int = -1

  val snapshotArchive = new FilesystemSnapshotArchive("/home/tony/snapshots")

  @Transactional def scanResource(checkResourceId: Int) {

    val processers: Seq[LinkCheckerProcessor] = Seq() // TODO inject

    resourceDAO.loadResourceById(checkResourceId).map { resource =>
      if (resource != null && !Strings.isNullOrEmpty(resource.getUrl)) {
        log.info("Checking: " + resource.getName + " (" + resource.getUrl + ")")
        val pageContent = httpCheck(resource)
        for (processor <- processers) {
          log.debug("Running processor: " + processor.getClass.toString)
          try {
            processor.process(resource, pageContent)
          }
          catch {
            case e: Exception => {
              log.error("An exception occured while running a link checker processor", e)
            }
          }
        }
        log.debug("Saving resource and updating snapshot")
        resource.setLastScanned(DateTime.now.toDate)
        if (pageContent != null) {
          snapshotArchive.put(new Snapshot(resource.getUrl, DateTime.now.toDate, pageContent))
        }
        contentUpdateService.update(resource)
        log.info("Finished linkchecking")
      }

      else {
        log.warn("Could not check resource with id #" + checkResourceId + " as it was not found in the database")
      }
    }
  }

  private def httpCheck(checkResource: Resource): String = {
    try {
      var pageContent: String = null
      val httpResult: HttpFetchResult = httpFetcher.httpFetch(checkResource.getUrl)
      checkResource.setHttpStatus(httpResult.getStatus)
      log.info("Http status for " + checkResource.getUrl + " set to: " + checkResource.getHttpStatus)
      if (httpResult.getStatus == HttpStatus.SC_OK) {
        pageContent = httpResult.readEncodedResponse("UTF-8")
        return pageContent
      }
      checkResource.setHttpStatus(httpResult.getStatus)
      return null
    }
    catch {
      case e: IllegalArgumentException => {
        log.error("Error while checking url: ", e)
      }
      case e: IOException => {
        log.error("Error while checking url: ", e)
      }
    }
    checkResource.setHttpStatus(CANT_CONNECT)
    return null
  }

}
