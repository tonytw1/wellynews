package nz.co.searchwellington.model

case class PublisherContentCount(publisher: Website, count: Long) {

  def getPublisherName: String = publisher.title.getOrElse(publisher.id)
  def getCount: Long = count

}
