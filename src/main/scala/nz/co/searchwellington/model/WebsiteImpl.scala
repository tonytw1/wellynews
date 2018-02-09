package nz.co.searchwellington.model

import java.util.Date

case class WebsiteImpl(override var id: Int = 0,
                       override var `type`: String = "",
                       override var title: Option[String] = None,
                       override var description: Option[String] = None,
                       override var page: Option[String] = None,
                       override var http_status: Int = 0,
                       override var date2: Option[Date] = None,
                       override var last_scanned2: Option[Date] = None,
                       override var last_changed2: Option[Date] = None,
                       override var live_time2: Option[Date] = None,
                       override var embargoed_until2: Option[Date] = None,
                       override var held2: Boolean = true,
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
