package nz.co.searchwellington.model.frontend

import java.io.Serializable
import java.util.{Date, List}

import nz.co.searchwellington.model.{Geocode, Tag}
import reactivemongo.api.bson.BSONObjectID
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
  val place: Option[Geocode]
  val held: Boolean
  val lastChanged: Option[Date]
  val lastScanned: Option[Date]
  val actions: Seq[Action]

  def getId: String = id

  final def getType: String = `type`

  final def getName: String = name

  final def getLabel: String = Option(name).getOrElse(id)

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

  def getPlace: Geocode = place.orNull

  def isHeld: Boolean = held

  def getImageUrl: String = null

  def getWebUrl: String = url

  def getAuthor: String = null

  def getLatLong: uk.co.eelpieconsulting.common.geo.model.LatLong = place.flatMap { p =>
    p.latitude.flatMap { latitude =>
      p.longitude.map { longitude =>
        new uk.co.eelpieconsulting.common.geo.model.LatLong(latitude, longitude)
      }
    }

  }.orNull

  def getLastScanned: Date = lastScanned.orNull

  def getLastChanged: Date = lastChanged.orNull

  def getActions: List[Action] = {
    import scala.collection.JavaConverters._
    actions.asJava
  }

  def getTaggingStatus: String = {
    if (handTags.nonEmpty) {
      "Tagged as: "
    } else {
      if (tags.nonEmpty) {
        "Automatically tagged as: "
      } else {
        "Not tagged"
      }
    }
  }

  def getTaggingsToShow: List[Tag] = {
    import scala.collection.JavaConverters._
    val tagsToShow = if (handTags.nonEmpty) {
      handTags
    } else {
      if (tags.nonEmpty) {
        tags
      } else {
        Seq.empty
      }
    }

    val withNoChildren = tagsToShow.map { t =>
      val children = tagsToShow.filter { c =>
        c.parent.contains(t._id)
      }.map(_._id).toSet
      TagWithChildren(t, children)
    }.filter(_.children.isEmpty).map(_.tag)

    withNoChildren.asJava
  }

  case class TagWithChildren(tag: Tag, children: Set[BSONObjectID])

}

