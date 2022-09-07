package nz.co.searchwellington.linkchecking

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Snapshot
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{Await, ExecutionContext}

@Component
class MongoSnapshotArchive @Autowired()(mongoRepository: MongoRepository) extends SnapshotArchive with ReasonableWaits {

  override def storeSnapshot(url: String, content: String)(implicit ec: ExecutionContext): Unit = {
    val snapshot = Snapshot(url, content, DateTime.now.toDate)
    Await.result(mongoRepository.saveSnapshot(snapshot), TenSeconds)
  }

  override def getLatestFor(url: String)(implicit ec: ExecutionContext): Option[String] = {
    Await.result(mongoRepository.getSnapshotByUrl(url), TenSeconds).map { snapshot =>
      snapshot.content
    }
  }

}
