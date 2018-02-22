package nz.co.searchwellington.model

trait PublishedResource extends Resource {

  var publisher: Option[Long]

  def getPublisher: Option[Long] = publisher

  def setPublisher(publisher: Int): Unit = this.publisher = Some(publisher)

  def getPublisherName: String = {
    //if (publisher != null) publisher.getName else null
    // publisher.getOrElse("")
    null
  }

}
