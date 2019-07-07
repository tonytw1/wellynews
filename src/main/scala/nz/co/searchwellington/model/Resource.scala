package nz.co.searchwellington.model

import java.util.Date

import reactivemongo.bson.BSONObjectID

trait Resource extends Tagged {
  val _id: BSONObjectID
  val id: String
  val `type`: String
  var title: Option[String]
  var description: Option[String]
  val page: Option[String]
  var http_status: Int
  var date: Option[Date]
  var last_scanned: Option[Date]
  var last_changed: Option[Date]
  var live_time: Option[Date]
  var embargoed_until: Option[Date]
  var held: Boolean
  var url_words: Option[String]
  var geocode: Option[Geocode]
  var owner: Option[BSONObjectID]
  val resource_tags: Seq[Tagging]

  def setDate(date: Date): Unit = {}  // TODO

  def setDescription(description: String): Unit = {}  // TODO this.description = description

  def setHttpStatus(httpStatus: Int): Unit = this.http_status = httpStatus

  def setLastScanned(lastScanned: Date): Unit = this.last_scanned = Some(lastScanned)

  def setLastChanged(lastChanged: Date): Unit = this.last_changed = Some(lastChanged)

  def setLiveTime(liveTime: Date): Unit = this.live_time = Some(liveTime)

  def setEmbargoedUntil(embargoedUntil: Date): Unit = this.embargoed_until = Some(embargoedUntil)

  def setUrlWords(urlWords: String): Unit = {} // TODO this.url_words = urlWords

  def setOwner(owner: User): Unit = {}  // TODO

  def setHeld(held: Boolean): Unit = this.held = held
}
