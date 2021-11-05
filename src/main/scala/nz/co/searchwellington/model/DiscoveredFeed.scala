package nz.co.searchwellington.model

import java.util.Date
import reactivemongo.api.bson.BSONObjectID

import java.util

case class DiscoveredFeed(_id: BSONObjectID = BSONObjectID.generate, url: String, occurrences: Seq[DiscoveredFeedOccurrence], firstSeen: Date) {

  def getUrl: String = url
  def getOccurrences: util.List[DiscoveredFeedOccurrence] = {
    import scala.collection.JavaConverters._
    occurrences.asJava
  }
  def getFirstSeen: Date = occurrences.headOption.map(_.seen).orNull  // TODO confirm ordering

}

case class DiscoveredFeedOccurrence(referencedFrom: String, seen: Date)