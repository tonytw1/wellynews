package nz.co.searchwellington.model.frontend

import java.io.Serializable
import java.util.{Date, List}

import uk.co.eelpieconsulting.common.geo.model.{LatLong, Place}
import uk.co.eelpieconsulting.common.views.rss.RssFeedable

@SerialVersionUID(1L)
class FrontendResource extends RssFeedable with Serializable {
  private var id: Int = 0
  private var urlWords: String = null
  private var `type`: String = null
  private var name: String = null
  private var url: String = null
  private var httpStatus: Int = 0
  private var date: Date = null
  private var description: String = null
  private var liveTime: Date = null
  private var tags: List[FrontendTag] = null
  private var handTags: List[FrontendTag] = null
  private var owner: String = null
  private var place: Place = null
  private var held: Boolean = false
  final protected var publisherName: String = null

  final def getPublisherName: String = {
    return publisherName
  }

  final def setPublisherName(publisherName: String) {
    this.publisherName = publisherName
  }

  def getId: Int = {
    return id
  }

  final def setId(id: Int) {
    this.id = id
  }

  final def getType: String = {
    return `type`
  }

  final def setType(`type`: String) {
    this.`type` = `type`
  }

  final def getName: String = {
    return name
  }

  final def setName(name: String) {
    this.name = name
  }

  final def getUrl: String = {
    return url
  }

  final def setUrl(url: String) {
    this.url = url
  }

  final def getHttpStatus: Int = {
    return httpStatus
  }

  final def setHttpStatus(httpStatus: Int) {
    this.httpStatus = httpStatus
  }

  final def getDate: Date = {
    return date
  }

  final def setDate(date: Date) {
    this.date = date
  }

  final def getDescription: String = {
    return description
  }

  final def setDescription(description: String) {
    this.description = description
  }

  final def getLiveTime: Date = {
    return liveTime
  }

  final def setLiveTime(liveTime: Date) {
    this.liveTime = liveTime
  }

  final def getTags: List[FrontendTag] = {
    return tags
  }

  final def setTags(tags: List[FrontendTag]) {
    this.tags = tags
  }

  final def getHandTags: List[FrontendTag] = {
    return handTags
  }

  final def setHandTags(handTags: List[FrontendTag]) {
    this.handTags = handTags
  }

  final def getOwner: String = {
    return owner
  }

  final def setOwner(owner: String) {
    this.owner = owner
  }

  def getUrlWords: String = {
    return urlWords
  }

  def setUrlWords(urlWords: String) {
    this.urlWords = urlWords
  }

  def getPlace: Place = {
    return place
  }

  def setPlace(place: Place) {
    this.place = place
  }

  def getLocation: String = {
    if (place != null && place.getLatLong != null) {
      return place.getLatLong.getLatitude + "," + place.getLatLong.getLongitude
    }
    return null
  }

  def isHeld: Boolean = {
    return held
  }

  def setHeld(held: Boolean) {
    this.held = held
  }

  def getHeadline: String = {
    return name
  }

  def getImageUrl: String = {
    return null
  }

  def getLatLong: LatLong = {
    return if (place != null) place.getLatLong else null
  }

  def getWebUrl: String = {
    return url
  }

  def getAuthor: String = {
    return null
  }
}
