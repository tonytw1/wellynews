package nz.co.searchwellington.model

trait PublishedResource extends Resource {

  var publisher: Option[Long]

  def getPublisher: Option[Long] = publisher
  def setPublisher(publisher: Long): Unit = this.publisher = Some(publisher)

}
