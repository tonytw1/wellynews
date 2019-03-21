package nz.co.searchwellington.model

import java.util.Date

import reactivemongo.bson.BSONObjectID

case class Feed(override var _id: Option[BSONObjectID] = None,
                override val id: String,
                override val `type`: String = "F",
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
                override var owner: Option[Int] = None,
                override val resource_tags: Seq[Tagging] = Seq(),
                var acceptance: FeedAcceptancePolicy = FeedAcceptancePolicy.IGNORE,
                var latestItemDate: Option[Date] = null,
                var lastRead: Option[Date] = null, // TODO
                var whakaoko_id: Option[String] = None,
                override var publisher: Option[BSONObjectID] = None) extends PublishedResource {

  def getAcceptancePolicy: FeedAcceptancePolicy = acceptance

  def setAcceptancePolicy(acceptancePolicy: FeedAcceptancePolicy): Unit = this.acceptance = acceptancePolicy

  def getLatestItemDate: Date = latestItemDate.getOrElse(null)

  def setLatestItemDate(latestPublicationDate: Date): Unit = {}

  def getLastRead: Date = lastRead.getOrElse(null)

  def setLastRead(lastRead: Date): Unit = {}

  def isScreenScraped: Boolean = page.map(p => p.startsWith("http://brownbag.wellington.gen.nz/")).getOrElse(false)

  def getWhakaokoId: Option[String] = whakaoko_id

  def setWhakaokoId(whakaokoId: String): Unit = this.whakaoko_id = Some(whakaokoId)

}

