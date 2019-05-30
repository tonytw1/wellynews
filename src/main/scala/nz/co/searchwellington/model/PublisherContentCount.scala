package nz.co.searchwellington.model

case class PublisherContentCount(publisherName: String, count: Long) {

  def getPublisherName: String = publisherName
  def getCount: Long = count

}
