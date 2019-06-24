package nz.co.searchwellington.model.frontend

import java.io.Serializable
import java.util.{Date, List}

import nz.co.searchwellington.model.Tag
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
  val tags: Seq[Tag]
  val handTags: Seq[Tag]
  val owner: String
  val place: Option[Place]
  val held: Boolean

  def getId: String = id

  final def getType: String = `type`

  final def getName: String = name

  final def getHeadline: String = name

  final def getUrl: String = url

  final def getHttpStatus: Int = httpStatus

  final def getDate: Date = date

  final def getDescription: String = description

  final def getLiveTime: Date = liveTime

  final def getTags: List[Tag] = {
    import scala.collection.JavaConverters._
    tags.asJava
  }

  final def getHandTags: List[Tag] = {
    import scala.collection.JavaConverters._
    handTags.asJava
  }

  final def getOwner: String = owner

  def getUrlWords: String = urlWords

  def getPlace: Place = place.orNull

  def getLocation: String = {
    place.flatMap { p =>
      p.latLong.map{ ll =>
        ll.latitude + "." + ll.longitude    // TODO display name
      }
    }.orNull
  }

  def isHeld: Boolean = held

  def getImageUrl: String = null

  def getLatLong: uk.co.eelpieconsulting.common.geo.model.LatLong = place.flatMap { p =>
    p.latLong.map { ll =>
      new uk.co.eelpieconsulting.common.geo.model.LatLong(ll.latitude, ll.longitude)
    }
  }.orNull

  def getWebUrl: String = url

  def getAuthor: String = null

}
