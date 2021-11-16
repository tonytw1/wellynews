package nz.co.searchwellington.model

import reactivemongo.api.bson.BSONObjectID

trait PublishedResource extends Resource {
  var publisher: Option[BSONObjectID]
  def getPublisher: Option[BSONObjectID] = publisher
  def setPublisher(publisher: Website): Unit = this.publisher = Some(publisher._id)
  def clearPublisher(): Unit = this.publisher = None
}
