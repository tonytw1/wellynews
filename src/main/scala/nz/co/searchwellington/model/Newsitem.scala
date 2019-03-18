package nz.co.searchwellington.model

import java.util.{Date, UUID}

case class Newsitem(override val id: String = UUID.randomUUID().toString,
                    override val `type`: String = "N",
                    override var title: Option[String] = None,
                    override var page: Option[String] = None,
                    override var http_status: Int = 0,
                    override var date: Option[Date] = None,
                    override var description: Option[String] = None,
                    override var last_scanned: Option[Date] = None,
                    override var last_changed: Option[Date] = None,
                    override var live_time: Option[Date] = None,
                    override var embargoed_until: Option[Date] = None,
                    override var held: Boolean = true,
                    override var url_words: Option[String] = None,
                    override var geocode: Option[Geocode] = None,
                    override var owner: Option[Int] = None,
                    override var publisher: Option[String] = None,
                    var feed: Option[String] = None,
                    var commentFeed: Option[Int] = None,
                    var image: Option[Int] = None,
                    var accepted2: Option[Date] = None,
                    var acceptedBy: Option[Int] = None) extends PublishedResource
