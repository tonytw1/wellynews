package nz.co.searchwellington.model

import java.util.Date

import reactivemongo.api.bson.BSONObjectID

case class DiscoveredFeed(_id: BSONObjectID = BSONObjectID.generate, url: String, referencedFrom: String, seen: Date) {

  def getUrl: String = url
  def getReferencedFrom = referencedFrom
  def getSeen: Date = seen

}