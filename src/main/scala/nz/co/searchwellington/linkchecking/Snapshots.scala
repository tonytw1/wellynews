package nz.co.searchwellington.linkchecking

import org.springframework.stereotype.Component

import scala.collection.mutable

@Component class InMemorySnapshotArchive() {

  private val snapshots: mutable.Map[String, String] = scala.collection.mutable.Map.empty

  def storeSnapshot(url: String, content: String): Option[String] = {
    snapshots.put(url, content)
  }

  def getLatestFor(url: String): Option[String] = snapshots.get(url)

}
