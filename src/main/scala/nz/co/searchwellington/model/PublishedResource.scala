package nz.co.searchwellington.model

trait PublishedResource extends Resource {
  var publisher: Option[Int]
  def getPublisher: Option[Int] = publisher

  def setPublisher(publisher: Int): Unit = this.publisher = Some(publisher)

  def getPublisherName: String = {
    //if (publisher != null) publisher.getName else null
    // publisher.getOrElse("")
    null
  }

}
