package nz.co.searchwellington.linkchecking

import scala.concurrent.ExecutionContext

trait SnapshotArchive {

  def storeHash(url: String, content: String)(implicit ec: ExecutionContext): Unit
  def getLatestHashFor(url: String)(implicit ec: ExecutionContext): Option[String]

}
