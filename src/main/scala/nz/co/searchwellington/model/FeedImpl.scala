package nz.co.searchwellington.model

import java.util.Date

import org.joda.time.DateTime

case class FeedImpl(override var id: Int = 0,
                    override var `type`: String = "",
                    override var name: String = "",
                    override var url: String = "",
                    override var httpStatus: Int = 0,
                    override var date: Date = DateTime.now.toDate,
                    override var description: String = "",
                    override var lastScanned: Date = null,
                    override var lastChanged: Date = null,
                    override var liveTime: Date = null,
                    override var embargoedUntil: Date = null,
                    override var held: Boolean = false,
                    override var urlWords: String = null,
                    override var geocode: Geocode = null,
                    override var owner: User = null,
                    var acceptancePolicy: FeedAcceptancePolicy = null,
                    var latestItemDate: Date = null,
                    var lastRead: Date = null,
                    var whakaokoId: String = null,
                    override var publisher: Website = null) extends PublishedResourceImpl with Feed {

  override def getType: String = "F"

  override def getAcceptancePolicy: FeedAcceptancePolicy = acceptancePolicy

  override def setAcceptancePolicy(acceptancePolicy: FeedAcceptancePolicy): Unit = this.acceptancePolicy = acceptancePolicy

  override def getLatestItemDate: Date = latestItemDate

  override def setLatestItemDate(latestPublicationDate: Date): Unit = this.latestItemDate = latestPublicationDate

  override def getLastRead: Date = lastRead

  override def setLastRead(lastRead: Date): Unit = this.lastRead = lastRead

  def isScreenScraped: Boolean = url.startsWith("http://brownbag.wellington.gen.nz/")

  override def getWhakaokoId: String = whakaokoId

  override def setWhakaokoId(whakaokoId: String): Unit = this.whakaokoId = whakaokoId

}
