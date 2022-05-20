package nz.co.searchwellington.model

import reactivemongo.api.bson.BSONObjectID

import java.util
import java.util.Date
import scala.jdk.CollectionConverters._

case class DiscoveredFeed(_id: BSONObjectID = BSONObjectID.generate, url: String, occurrences: Seq[DiscoveredFeedOccurrence], firstSeen: Date, publisher: Option[BSONObjectID]) {
  def getUrl: String = url
  def getOccurrences: util.List[DiscoveredFeedOccurrence] = occurrences.asJava
  def getFirstSeen: Date = occurrences.headOption.map(_.seen).orNull  // TODO confirm ordering
}

case class DiscoveredFeedOccurrence(referencedFrom: String, seen: Date) {
  def getReferencedFrom: String = referencedFrom
  def getSeen: Date = seen
}
