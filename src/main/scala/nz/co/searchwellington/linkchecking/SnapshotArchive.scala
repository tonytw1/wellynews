package nz.co.searchwellington.linkchecking

import scala.concurrent.ExecutionContext

trait SnapshotArchive {

  def storeSnapshot(url: String, content: String)(implicit ec: ExecutionContext): Unit
  def getLatestFor(url: String)(implicit ec: ExecutionContext): Option[String]

}
