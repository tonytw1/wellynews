package nz.co.searchwellington.model

case class PublisherContentCount(publisher: Website, count: Long) {

  def getPublisher: Website = publisher
  def getCount: Long = count

}
