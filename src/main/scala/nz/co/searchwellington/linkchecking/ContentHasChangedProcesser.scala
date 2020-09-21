package nz.co.searchwellington.linkchecking

import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.utils.UrlFilters
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.archiving.SnapshotArchive

import scala.concurrent.{ExecutionContext, Future}

@Component class ContentHasChangedProcesser @Autowired()(snapshotArchive: SnapshotArchive) extends LinkCheckerProcessor {

  private val log = Logger.getLogger(classOf[ContentHasChangedProcesser])

  override def process(checkResource: Resource, pageContent: String, seen: DateTime)(implicit ec: ExecutionContext): Future[Boolean] = {
    checkForChangeUsingSnapshots(checkResource, pageContent)
    Future.successful(false)
  }

  // TODO cleaning and filtering?
  private def checkForChangeUsingSnapshots(checkResource: Resource, after: String) = {
    log.debug("Comparing content before and after snapshots from content change.")
    val snapshotBeforeHttpCheck = snapshotArchive.getLatestFor(checkResource.page)
    val pageContentBeforeHttpCheck = if (snapshotBeforeHttpCheck != null) snapshotBeforeHttpCheck.getBody else null
    if (contentHasChanged(pageContentBeforeHttpCheck, after)) {
      log.info("Change in content checksum detected. Setting last changed.")
      checkResource.setLastChanged(new DateTime().toDate)
    }

  }

  private def contentHasChanged(before: String, after: String): Boolean = { // TODO use options
    if (before != null && after != null) {
      !(UrlFilters.stripHtml(after) == UrlFilters.stripHtml(before))
    } else {
      val bothAreNull = (before == null) && (after == null)
      !bothAreNull
    }
  }

}
