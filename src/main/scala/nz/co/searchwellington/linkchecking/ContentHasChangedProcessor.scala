package nz.co.searchwellington.linkchecking

import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.utils.UrlFilters
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class ContentHasChangedProcessor @Autowired()(snapshotArchive: SnapshotArchive) extends LinkCheckerProcessor {

  private val log = LogFactory.getLog(classOf[ContentHasChangedProcessor])

  override def process(checkResource: Resource, pageContent: Option[String], seen: DateTime)(implicit ec: ExecutionContext): Future[Boolean] = {
    checkForChangeUsingSnapshots(checkResource, pageContent, seen)
    Future.successful(false)
  }

  // TODO cleaning and filtering?
  private def checkForChangeUsingSnapshots(checkResource: Resource, pageContent: Option[String], seen: DateTime)(implicit ec: ExecutionContext) = {
    log.debug("Comparing content before and after snapshots from content change.")
    val snapshotBeforeHttpCheck = snapshotArchive.getLatestFor(checkResource.page)

    if (contentHasChanged(snapshotBeforeHttpCheck, pageContent)) {
      log.info("Change in content checksum detected. Setting last changed and storing a snapshot.")
      checkResource.setLastChanged(seen.toDate)
      snapshotArchive.storeSnapshot(checkResource.page, pageContent.get)
    }
  }

  private def contentHasChanged(before: Option[String], after: Option[String]): Boolean = {
    (before, after) match {
      case (Some(before), Some(after)) => !(UrlFilters.stripHtml(before) == UrlFilters.stripHtml(after))
      case _ => false
    }
  }

}
