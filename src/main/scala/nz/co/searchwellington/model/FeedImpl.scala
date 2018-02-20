package nz.co.searchwellington.model

import java.util.Date

import org.joda.time.format.ISODateTimeFormat

case class FeedImpl(override var id: Int = 0,
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
                    override var owner: Option[Int] = None,
                    var acceptance: String = "IGNORE",
                    var latestItemDate2: Option[Date] = null,
                    var lastRead2: Option[Date] = null, // TODO
                    var whakaoko_id: String = null,
                    override var publisher: Option[Int] = None) extends PublishedResource with Feed {

  override def getType: String = "F"

  override def getAcceptancePolicy: String = acceptance

  override def setAcceptancePolicy(acceptancePolicy: String): Unit = this.acceptance = acceptancePolicy

  override def getLatestItemDate: Date = latestItemDate2.getOrElse(null)

  override def setLatestItemDate(latestPublicationDate: Date): Unit = {}

  override def getLastRead: Date = lastRead2.getOrElse(null)

  override def setLastRead(lastRead: Date): Unit = {}

  def isScreenScraped: Boolean = page.map(p => p.startsWith("http://brownbag.wellington.gen.nz/")).getOrElse(false)

  override def getWhakaokoId: String = whakaoko_id

  override def setWhakaokoId(whakaokoId: String): Unit = this.whakaoko_id = whakaokoId

}
