package nz.co.searchwellington.linkchecking.processors

import nz.co.searchwellington.linkchecking.{PageContentHasher, SnapshotArchive}
import nz.co.searchwellington.model.Resource
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class ContentHasChangedProcessor @Autowired()(snapshotArchive: SnapshotArchive, pageContentHasher: PageContentHasher) extends LinkCheckerProcessor {

  private val log = LogFactory.getLog(classOf[ContentHasChangedProcessor])

  override def process(checkResource: Resource, pageContent: Option[String], seen: DateTime)(implicit ec: ExecutionContext): Future[Resource] = {
    checkForChangeUsingSnapshots(checkResource, pageContent, seen)
    Future.successful(checkResource)
  }

  // TODO cleaning and filtering?
  private def checkForChangeUsingSnapshots(checkResource: Resource, pageContent: Option[String], seen: DateTime)(implicit ec: ExecutionContext): Unit = {
    log.debug("Comparing content before and after snapshots from content change.")
    pageContent.foreach { pageContent =>
      val previousHash: Option[String] = snapshotArchive.getLatestHashFor(checkResource.page)
      val currentHash: String = pageContentHasher.hashPageContent(pageContent)
      if (contentHasChanged(previousHash, currentHash)) {
        log.info(s"Change in content checksum detected for ${checkResource.page} ($previousHash / $currentHash). Setting last changed and storing a snapshot.")
        checkResource.setLastChanged(seen.toDate)
      }
      if (!previousHash.contains(currentHash)) {
        log.info(s"Setting stored hash for ${checkResource.page} to $currentHash")
        snapshotArchive.storeHash(checkResource.page, currentHash)
      }
    }
  }

  private def contentHasChanged(maybePreviousHash: Option[String], currentHash: String): Boolean = {
    maybePreviousHash match {
      case Some(previousHash) => !(previousHash == currentHash)
      case _ => false
    }
  }

}
