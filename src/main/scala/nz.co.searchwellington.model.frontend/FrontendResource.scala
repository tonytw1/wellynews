package nz.co.searchwellington.model.frontend

import java.io.Serializable
import java.util.{Date, List}

import uk.co.eelpieconsulting.common.geo.model.{LatLong, Place}
import uk.co.eelpieconsulting.common.views.rss.RssFeedable

trait FrontendResource extends RssFeedable with Serializable {
  val id: String
  val urlWords: String
  val `type`: String
  val name: String
  val url: String
  val httpStatus: Int
  val date: Date
  val description: String
  val liveTime: Date
  val tags: List[FrontendTag]
  val handTags: List[FrontendTag]
  val owner: String
  val place: Option[Place]
  val held: Boolean

  def getId: String = id

  final def getType: String = {
    `type`
  }

  final def getName: String = {
    name
  }

  final def getHeadline: String = {
    name
  }

  final def getUrl: String = {
    url
  }

  final def getHttpStatus: Int = {
    httpStatus
  }

  final def getDate: Date = {
    date
  }

  final def getDescription: String = {
    description
  }

  final def getLiveTime: Date = {
    liveTime
  }

  final def getTags: List[FrontendTag] = {
    tags
  }

  final def getHandTags: List[FrontendTag] = {
    handTags
  }

  final def getOwner: String = {
    owner
  }

  def getUrlWords: String = {
    urlWords
  }

  def getPlace: Place = place.orNull

  def getLocation: String = {
    place.flatMap { p =>
      if (p.getLatLong != null) {
        Some(p.getLatLong.getLatitude + "," + p.getLatLong.getLongitude)
      } else {
        None
      }
    }.orNull
  }

  def isHeld: Boolean = {
    held
  }

  def getImageUrl: String = {
    null
  }

  def getLatLong: LatLong = {
    place.map { p =>
      p.getLatLong
    }.orNull
  }

  def getWebUrl: String = {
    url
  }

  def getAuthor: String = {
    null
  }

}
