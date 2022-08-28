package nz.co.searchwellington.model.frontend

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.{JsonFormat, JsonInclude}
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.model.geo.Geocode
import reactivemongo.api.bson.BSONObjectID
import uk.co.eelpieconsulting.common.geo.model
import uk.co.eelpieconsulting.common.views.rss.RssFeedable

import java.util
import java.util.Date
import scala.jdk.CollectionConverters._

@JsonInclude(Include.NON_NULL)
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
  val tags: Option[Seq[Tag]]
  val handTags: Option[Seq[Tag]]
  val owner: String
  val geocode: Option[Geocode]
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

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  final def getDate: Date = date

  final def getDescription: String = description

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  final def getLiveTime: Date = liveTime

  final def getTags: util.List[Tag] = tags.map(_.asJava).orNull

  final def getHandTags: util.List[Tag] = handTags.map(_.asJava).orNull

  final def getOwner: String = owner

  def getUrlWords: String = urlWords

  def getGeocode: Geocode = geocode.orNull

  def isHeld: Boolean = held

  def getImageUrl: String = null

  def getWebUrl: String = url

  def getAuthor: String = null

  def getLatLong: uk.co.eelpieconsulting.common.geo.model.LatLong = geocode.flatMap { p =>
    p.latLong.map { ll =>
      new model.LatLong(ll.latitude, ll.longitude)
    }
  }.orNull

  @Override
  override def getCategories: util.List[String] = {
    tags.map { tags =>
      tags.map(_.name)
    }.getOrElse(Seq.empty).asJava
  }

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  def getLastScanned: Date = lastScanned.orNull

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  def getLastChanged: Date = lastChanged.orNull

  def getActions: util.List[Action] = actions.asJava

  def getTaggingStatus: String = {
    (for {
      handTags <- handTags
      tags <- tags
    } yield {
      if (handTags.nonEmpty) {
        "Tagged as: "
      } else {
        if (tags.nonEmpty) {
          "Automatically tagged as: "
        } else {
          "Not tagged"
        }
      }
    }).orNull
  }

  def getTaggingsToShow: util.List[Tag] = {
    (for {
      handTags <- handTags
      tags <- tags
    } yield {
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
    }).orNull
  }

  case class TagWithChildren(tag: Tag, children: Set[BSONObjectID])

  override def getFeatureName: String = {
    geocode.flatMap { place =>
      place.address
    }.orNull
  }

}

