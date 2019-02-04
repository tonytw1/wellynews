package nz.co.searchwellington.model

import java.util.Date

case class Website(override var id: Int = 0,
                   override val `type`: String = "W",
                   override var title: Option[String] = None,
                   override var description: Option[String] = None,
                   override var page: Option[String] = None,
                   override var http_status: Int = 0,
                   override var date: Option[Date] = None,
                   override var last_scanned: Option[Date] = None,
                   override var last_changed: Option[Date] = None,
                   override var live_time: Option[Date] = None,
                   override var embargoed_until: Option[Date] = None,
                   override var held2: Boolean = true,
                   override var url_words: Option[String] = None,
                   override var geocode: Option[Geocode] = None,
                   override var owner: Option[Int] = None
                   // var feeds: Set[Feed] = Set(),
                   // var watchlist: Set[Watchlist] = Set()
                  ) extends Resource {

  def getFeeds: Set[Feed] = Set()

  // TODO
  def setFeeds(feeds: Set[Feed]): Unit = {} // TODO
  
  def getWatchlist: Set[Watchlist] = Set()

  // TODO
  def setWatchlist(watchlist: Set[Watchlist]): Unit = {} // TODO

}
