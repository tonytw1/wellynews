package nz.co.searchwellington.linkchecking

import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.utils.UrlFilters
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class ContentHasChangedProcesser @Autowired()(snapshotArchive: InMemorySnapshotArchive) extends LinkCheckerProcessor {

  private val log = LogFactory.getLog(classOf[ContentHasChangedProcesser])

  override def process(checkResource: Resource, pageContent: Option[String], seen: DateTime)(implicit ec: ExecutionContext): Future[Boolean] = {
    checkForChangeUsingSnapshots(checkResource, pageContent, seen)
    Future.successful(false)
  }

  // TODO cleaning and filtering?
  private def checkForChangeUsingSnapshots(checkResource: Resource, pageContent: Option[String], seen: DateTime) = {
    log.debug("Comparing content before and after snapshots from content change.")
    val snapshotBeforeHttpCheck = snapshotArchive.getLatestFor(checkResource.page)

    if (contentHasChanged(snapshotBeforeHttpCheck, pageContent)) {
      log.info("Change in content checksum detected. Setting last changed and storing a snapshot.")
      checkResource.setLastChanged(seen.toDate)
      snapshotArchive.storeSnapshot(checkResource.page, pageContent.get)
    }
  }

  private def contentHasChanged(before: Option[String], after: Option[String]): Boolean = {
    if (before.isEmpty && after.isEmpty) {
      false
    } else if (before.isEmpty && after.nonEmpty) {
        true
    } else if (before.nonEmpty && after.nonEmpty) {
        !(UrlFilters.stripHtml(after.get) == UrlFilters.stripHtml(before.get))
    } else {
      false
    }
  }

}
