package nz.co.searchwellington.linkchecking

import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.utils.UrlFilters
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.archiving.FilesystemSnapshotArchive

@Component class ContentHasChangedProcesser() extends LinkCheckerProcessor {

  private val log = Logger.getLogger(classOf[ContentHasChangedProcesser])

  private val snapshotArchive = new FilesystemSnapshotArchive("/home/tony/snapshots")

  override def process(checkResource: Resource, pageContent: String): Unit = checkForChangeUsingSnapshots(checkResource, pageContent)

  // TODO cleaning and filtering?
  private def checkForChangeUsingSnapshots(checkResource: Resource, after: String) = {
    log.debug("Comparing content before and after snapshots from content change.")
    checkResource.page.map { p =>
      val snapshotBeforeHttpCheck = snapshotArchive.getLatestFor(p)
      val pageContentBeforeHttpCheck = if (snapshotBeforeHttpCheck != null) snapshotBeforeHttpCheck.getBody else null
      if (contentHasChanged(pageContentBeforeHttpCheck, after)) {
        log.info("Change in content checksum detected. Setting last changed.")
        checkResource.setLastChanged(new DateTime().toDate)
      }
    }
  }

  private def contentHasChanged(before: String, after: String): Boolean = {
    if (before != null && after != null) {
      !(UrlFilters.stripHtml(after) == UrlFilters.stripHtml(before))
    } else {
      val bothAreNull = (before == null) && (after == null)
      !bothAreNull
    }
  }

}
