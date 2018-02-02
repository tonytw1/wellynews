package nz.co.searchwellington.model

trait PublishedResource extends Resource {
  def getPublisher: Website

  def setPublisher(publisher: Website): Unit

  def getPublisherName: String
}
