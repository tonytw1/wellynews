package nz.co.searchwellington.model

import reactivemongo.bson.BSONObjectID

trait PublishedResource extends Resource {
  var publisher: Option[BSONObjectID]
  def getPublisher: Option[BSONObjectID] = publisher
  def setPublisher(publisher: BSONObjectID): Unit = this.publisher = Some(publisher)
}
