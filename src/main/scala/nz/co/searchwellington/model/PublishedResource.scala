package nz.co.searchwellington.model

import reactivemongo.api.bson.BSONObjectID

trait PublishedResource extends Resource {
  var publisher: Option[BSONObjectID]
  def getPublisher: Option[BSONObjectID] = publisher
  def clearPublisher(): Unit = this.publisher = None
}
