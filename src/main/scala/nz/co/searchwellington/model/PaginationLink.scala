package nz.co.searchwellington.model

case class PaginationLink(page: Long, url: String) {

  def getPage: Long = page
  def getUrl: String = url

}
