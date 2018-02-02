package nz.co.searchwellington.model

@SerialVersionUID(1L)
abstract class PublishedResourceImpl extends Resource with PublishedResource {
  protected var publisher: Website = null

  override def getPublisher: Website = publisher

  override def setPublisher(publisher: Website): Unit = this.publisher = publisher

  override def getPublisherName: String = {
    if (publisher != null) return publisher.getName
    null
  }
}
