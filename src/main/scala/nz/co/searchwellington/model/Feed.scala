package nz.co.searchwellington.model

import java.util.Date

case class Feed(override var id: Int = 0,
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
                override var geocode2: Option[Geocode] = None,
                override var owner: Option[Int] = None,
                var acceptance: String = "IGNORE",
                var latestItemDate2: Option[Date] = null,
                var lastRead2: Option[Date] = null, // TODO
                var whakaoko_id: String = null,
                override var publisher: Option[Long] = None) extends PublishedResource {

  override def getType: String = "F"

  def getAcceptancePolicy: String = acceptance

  def setAcceptancePolicy(acceptancePolicy: String): Unit = this.acceptance = acceptancePolicy

  def getLatestItemDate: Date = latestItemDate2.getOrElse(null)

  def setLatestItemDate(latestPublicationDate: Date): Unit = {}

  def getLastRead: Date = lastRead2.getOrElse(null)

  def setLastRead(lastRead: Date): Unit = {}

  def isScreenScraped: Boolean = page.map(p => p.startsWith("http://brownbag.wellington.gen.nz/")).getOrElse(false)

  def getWhakaokoId: String = whakaoko_id

  def setWhakaokoId(whakaokoId: String): Unit = this.whakaoko_id = whakaokoId

}

