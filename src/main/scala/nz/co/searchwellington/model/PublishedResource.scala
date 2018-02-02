package nz.co.searchwellington.model

trait PublishedResource extends Resource {
  var publisher: Website

  def getPublisher: Website = publisher

  def setPublisher(publisher: Website): Unit = this.publisher = publisher

  def getPublisherName: String = {
    if (publisher != null) publisher.getName else null
  }

}
