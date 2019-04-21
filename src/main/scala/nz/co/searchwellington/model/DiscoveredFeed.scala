package nz.co.searchwellington.model

import reactivemongo.bson.BSONObjectID
import scala.collection.JavaConverters._

import scala.collection.mutable

case class DiscoveredFeed(_id: BSONObjectID = BSONObjectID.generate, url: String, references: mutable.Set[String] = mutable.Set.empty) {

  def getUrl: String = url

  def getReferences: java.util.Set[String] = references.asJava

}