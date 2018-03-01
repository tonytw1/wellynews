package nz.co.searchwellington.model

import java.util.Date

case class Newsitem(override var id: Int = 0,
                    override val `type`: String = "N",
                    override var title: Option[String] = None,
                    override var page: Option[String] = None,
                    override var http_status: Int = 0,
                    override var date2: Option[Date] = None,
                    override var description: Option[String] = None,
                    override var last_scanned2: Option[Date] = None,
                    override var last_changed2: Option[Date] = None,
                    override var live_time2: Option[Date] = None,
                    override var embargoed_until2: Option[Date] = None,
                    override var held2: Boolean = true,
                    override var url_words: Option[String] = None,
                    override var geocode: Option[Geocode] = None,
                    override var owner: Option[Int] = None,
                    override var publisher: Option[Long] = None,
                    var feed: Option[Int] = None,
                    var commentFeed: Option[Int] = None,
                    var image: Option[Int] = None,
                    var accepted2: Option[Date] = None,
                    var acceptedBy: Option[Int] = None) extends PublishedResource
