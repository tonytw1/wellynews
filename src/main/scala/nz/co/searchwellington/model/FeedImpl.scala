package nz.co.searchwellington.model

import java.util.Date

import org.joda.time.format.ISODateTimeFormat

case class FeedImpl(override var id: Int = 0,
                    override var `type`: String = "",
                    override var title: Option[String] = None,
                    override var page: Option[String] = None,
                    override var http_status: Int = 0,
                    override var date: Option[String] = None,
                    override var description: Option[String] = None,
                    override var last_scanned: Option[String] = None,
                    override var last_changed: Option[String] = None,
                    override var live_time: Option[String] = None,
                    override var embargoed_until: Option[String] = None,
                    override var held: Int = 0,
                    override var url_words: Option[String] = None,
                    override var geocode: Option[Int] = None,
                    override var owner: Option[Int] = None,
                    var acceptancePolicy: FeedAcceptancePolicy = null,
                    var latestItemDate: String = null,
                    var lastRead: String = null,
                    var whakaokoId: String = null,
                    override var publisher: Website = null) extends PublishedResource with Feed {

  override def getType: String = "F"

  override def getAcceptancePolicy: FeedAcceptancePolicy = acceptancePolicy

  override def setAcceptancePolicy(acceptancePolicy: FeedAcceptancePolicy): Unit = this.acceptancePolicy = acceptancePolicy

  override def getLatestItemDate: Date = ISODateTimeFormat.dateParser().parseDateTime(latestItemDate).toDate // TODO

  override def setLatestItemDate(latestPublicationDate: Date): Unit = {}

  override def getLastRead: Date = ISODateTimeFormat.dateParser().parseDateTime(lastRead).toDate // TODO

  override def setLastRead(lastRead: Date): Unit = {}

  def isScreenScraped: Boolean = page.map(p => p.startsWith("http://brownbag.wellington.gen.nz/")).getOrElse(false)

  override def getWhakaokoId: String = whakaokoId

  override def setWhakaokoId(whakaokoId: String): Unit = this.whakaokoId = whakaokoId

}
