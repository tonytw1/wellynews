package nz.co.searchwellington.model

trait PublishedResource extends Resource {
  var publisher: Option[String]
  def getPublisher: Option[String] = publisher
  def setPublisher(publisher: String): Unit = this.publisher = Some(publisher)
}
