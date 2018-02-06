package nz.co.searchwellington.model

case class Watchlist(
                      override var id: Int = 0,
                      override var `type`: String = "",
                      override var title: Option[String] = None,
                      override var page: Option[String] = None,
                      override var http_status: Int = 0,
                      override var date: Option[String] = None,
                      override var description: Option[String] = None,
                      override var last_scanned: Option[String] = None,
                      override var last_changed: Option[String] = None,
                      override var live_time:  Option[String] = None,
                      override var embargoed_until: Option[String] = None,
                      override var held: Int = 0,
                      override var url_words: Option[String] = None,
                      override var geocode: Option[Int] = None,
                      override var owner: Option[Int] = None,
                      override var publisher: Option[Int] = None
                    ) extends PublishedResource {

  override def getType = "L"

}
