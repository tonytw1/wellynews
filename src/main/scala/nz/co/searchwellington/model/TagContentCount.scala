package nz.co.searchwellington.model

case class TagContentCount(tag: Tag, count: Long) {

  def getTag: Tag = tag
  def getCount: Long = count

}
