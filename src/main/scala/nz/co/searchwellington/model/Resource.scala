package nz.co.searchwellington.model

import java.util.Date

trait Resource {
  var id: Int
  val `type`: String
  var title: Option[String]
  var description: Option[String]
  var page: Option[String]
  var http_status: Int
  var date: Option[Date]
  var last_scanned: Option[Date]
  var last_changed: Option[Date]
  var live_time: Option[Date]
  var embargoed_until: Option[Date]
  var held2: Boolean
  var url_words: Option[String]
  var geocode: Option[Geocode]
  var owner: Option[Int]

  def setDate(date: Date): Unit = {}  // TODO

  def setDescription(description: String): Unit = {}  // TODO this.description = description

  def setId(id: Int): Unit = this.id = id

  def setName(name: String): Unit = {} // this.title = name

  def setUrl(url: String) = this.page = Some(url)

  def setHttpStatus(httpStatus: Int): Unit = this.http_status = httpStatus

  def setLastScanned(lastScanned: Date): Unit = {}  // TODO

  def setLastChanged(lastChanged: Date): Unit = {}

  def setLiveTime(liveTime: Date): Unit = {}

  def setEmbargoedUntil(embargoedUntil: Date): Unit = {}

  def setUrlWords(urlWords: String): Unit = {} // TODO this.url_words = urlWords

  def setOwner(owner: User): Unit = {}  // TODO

  def setHeld(held: Boolean): Unit = this.held2 = held

}
