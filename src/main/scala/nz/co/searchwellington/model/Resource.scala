package nz.co.searchwellington.model

import java.util.Date

import org.joda.time.format.ISODateTimeFormat

trait Resource {
  var id: Int
  var `type`: String
  var title: Option[String]
  var description: Option[String]
  var page: Option[String]
  var http_status: Int
  var date2: Option[Date]
  var last_scanned2: Option[Date]
  var last_changed2: Option[Date]
  var live_time2: Option[Date]
  var embargoed_until2: Option[Date]
  var held2: Boolean
  var url_words: Option[String]
  var geocode: Option[Geocode]
  var owner: Option[Int]

  def getDate: Date = date2.getOrElse(null)
  def setDate(date: Date): Unit = {}  // TODO

  def getDescription: String = description.getOrElse("")
  def setDescription(description: String): Unit = {}  // TODO this.description = description

  def getId: Int = id
  def setId(id: Int): Unit = this.id = id

  def getName: String = title.getOrElse(null)
  def setName(name: String): Unit = {} // this.title = name

  def name(name: String): Resource = {
    // this.title = name
    this
  }

  def getType: String = `type`

  def getUrl: String = page.getOrElse(null)
  def setUrl(url: String) = this.page = Some(url)

  def getHttpStatus: Int = http_status
  def setHttpStatus(httpStatus: Int): Unit = this.http_status = httpStatus

  def getLastScanned: Date = last_scanned2.getOrElse(null)
  def setLastScanned(lastScanned: Date): Unit = {}  // TODO

  def getLastChanged: Date = last_changed2.getOrElse(null)
  def setLastChanged(lastChanged: Date): Unit = {}

  def getLiveTime: Date = live_time2.getOrElse(null)
  def setLiveTime(liveTime: Date): Unit = {}

  def getEmbargoedUntil: Date = embargoed_until2.getOrElse(null)
  def setEmbargoedUntil(embargoedUntil: Date): Unit = {}

  def getUrlWords: String = url_words.getOrElse(null)
  def setUrlWords(urlWords: String): Unit = {} // TODO this.url_words = urlWords

  def getOwner: User = null // TODO
  def setOwner(owner: User): Unit = {}  // TODO

  def isHeld: Boolean = held2
  def setHeld(held: Boolean): Unit = this.held2 = held

  final def getOwnerId: Integer = {
    // TODO if (owner != null) return owner.getId
    null
  }

}
