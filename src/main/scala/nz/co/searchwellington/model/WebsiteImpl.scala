package nz.co.searchwellington.model

case class WebsiteImpl(override var id: Int = 0,
                       override var `type`: String = "",
                       override var title: Option[String] = None,
                       override var page: Option[String] = None,
                       override var http_status: Int = 0,
                       override var date: String = null,
                       override var description: Option[String] = None,
                       override var last_scanned: Option[String] = None,
                       override var last_changed: Option[String] = None,
                       override var live_time: Option[String] = None,
                       override var embargoed_until: Option[String] = None,
                       override var held: Int = 0,
                       override var url_words: Option[String] = None,
                       override var geocode: Option[Int] = None,
                       override var owner: Option[Int] = None
                       // var feeds: Set[Feed] = Set(),
                       // var watchlist: Set[Watchlist] = Set()
                      ) extends Website {

  def getFeeds: Set[Feed] = Set() // TODO
  def setFeeds(feeds: Set[Feed]): Unit = {} // TODO

  override def getType: String = "W"

  def getWatchlist: Set[Watchlist] = Set()  // TODO
  def setWatchlist(watchlist: Set[Watchlist]): Unit = {}  // TODO

}
