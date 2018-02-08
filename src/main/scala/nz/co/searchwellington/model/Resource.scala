package nz.co.searchwellington.model

import java.util.Date

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

trait Resource {
  var id: Int

  var `type`: String

  var title: Option[String]

  var page: Option[String]

  var http_status: Int

  var date: Option[String]

  var description: Option[String]

  var last_scanned: Option[String]

  var last_changed: Option[String]

  var live_time: Option[String]

  var embargoed_until: Option[String]

  var held: Int

  var url_words: Option[String]

  var geocode: Option[Int]

  var owner: Option[Int]

  def getDate: Date = date.map(d => DateTime.now.toDate).getOrElse(null) // TODO

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

  def getLastScanned: Date = last_scanned.map(d => ISODateTimeFormat.dateParser().parseDateTime(d).toDate).getOrElse(null) // TODO

  def setLastScanned(lastScanned: Date): Unit = {}  // TODO

  def getLastChanged: Date = last_changed.map(d => ISODateTimeFormat.dateParser().parseDateTime(d).toDate).getOrElse(null) // TODO

  def setLastChanged(lastChanged: Date): Unit = {}

  def getLiveTime: Date = live_time.map(d => ISODateTimeFormat.dateParser().parseDateTime(d).toDate).getOrElse(null) // TODO

  def setLiveTime(liveTime: Date): Unit = {}

  def getEmbargoedUntil: Date = embargoed_until.map(d => ISODateTimeFormat.dateParser().parseDateTime(d).toDate).getOrElse(null) // TODO

  def setEmbargoedUntil(embargoedUntil: Date): Unit = {}

  def getGeocode: Geocode = null  // TODO

  def setGeocode(geocode: Geocode): Unit = {} // TODO

  def getUrlWords: String = url_words.getOrElse(null)

  def setUrlWords(urlWords: String): Unit = {} // TODO this.url_words = urlWords

  def getOwner: User = null // TODO

  def setOwner(owner: User): Unit = {}  // TODO

  def isHeld: Boolean = held == 1

  def setHeld(held: Boolean): Unit = this.held = 0  // TODO

  final def getOwnerId: Integer = {
    // TODO if (owner != null) return owner.getId
    null
  }

}




