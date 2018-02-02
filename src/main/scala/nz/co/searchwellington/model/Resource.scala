package nz.co.searchwellington.model

import java.util.Date

trait Resource {
  var id: Int

  var `type`: String

  var name: String

  var url: String

  var httpStatus: Int

  var date: Date

  var description: String

  var lastScanned: Date

  var lastChanged: Date

  var liveTime: Date

  var embargoedUntil: Date

  var held: Boolean

  var urlWords: String

  var geocode: Geocode

  var owner: User

  def getDate: Date = date

  def setDate(date: Date): Unit = this.date = date

  def getDescription: String = description

  def setDescription(description: String): Unit = this.description = description

  def getId: Int = id

  def setId(id: Int): Unit = this.id = id

  def getName: String = name

  def setName(name: String): Unit = this.name = name

  def name(name: String): Resource = {
    this.name = name
    this
  }

  def getType: String = `type`

  def getUrl: String = url

  def getHttpStatus: Int = httpStatus

  def setHttpStatus(httpStatus: Int): Unit = this.httpStatus = httpStatus

  def getLastScanned: Date = lastScanned

  def setLastScanned(lastScanned: Date): Unit = this.lastScanned = lastScanned

  def getLastChanged: Date = lastChanged

  def setLastChanged(lastChanged: Date): Unit = this.lastChanged = lastChanged

  def getLiveTime: Date = liveTime

  def setLiveTime(liveTime: Date): Unit = this.liveTime = liveTime

  def getEmbargoedUntil: Date = embargoedUntil

  def setEmbargoedUntil(embargoedUntil: Date): Unit = this.embargoedUntil = embargoedUntil

  def getGeocode: Geocode = geocode

  def setGeocode(geocode: Geocode): Unit = this.geocode = geocode

  def getUrlWords: String = urlWords

  def setUrlWords(urlWords: String): Unit = this.urlWords = urlWords

  def getOwner: User = owner

  def setOwner(owner: User): Unit = this.owner = owner

  def isHeld: Boolean = held

  def setHeld(held: Boolean): Unit = this.held = held

  final def getOwnerId: Integer = {
    if (owner != null) return owner.getId
    null
  }

}




