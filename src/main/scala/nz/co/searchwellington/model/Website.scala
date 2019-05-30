package nz.co.searchwellington.model

import java.util.{Date, UUID}

import reactivemongo.bson.BSONObjectID

case class Website(override val _id: BSONObjectID = BSONObjectID.generate,
                   override val id: String = UUID.randomUUID().toString,
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
                   override var held: Boolean = true,
                   override var url_words: Option[String] = None,
                   override var geocode: Option[Geocode] = None,
                   override var owner: Option[BSONObjectID] = None,
                   override val resource_tags: Seq[Tagging] = Seq()
                   // var feeds: Set[Feed] = Set(),
                   // var watchlist: Set[Watchlist] = Set()
                  ) extends Resource {

  def getTitle: String = title.getOrElse(id)

  def getFeeds: Set[Feed] = Set()

  // TODO
  def setFeeds(feeds: Set[Feed]): Unit = {} // TODO

  def getWatchlist: Set[Watchlist] = Set()

  // TODO
  def setWatchlist(watchlist: Set[Watchlist]): Unit = {} // TODO

}
