package nz.co.searchwellington.model

import com.google.common.base.{Splitter, Strings}

import java.util.UUID
import reactivemongo.api.bson.BSONObjectID

import scala.jdk.CollectionConverters.IterableHasAsScala

case class Tag(_id: BSONObjectID = BSONObjectID.generate,
               id: String = UUID.randomUUID.toString,
               name: String = "",
               display_name: String = "",
               parent: Option[BSONObjectID] = None,
               hidden: Boolean = false,
               featured: Boolean = false,
               geocode: Option[Geocode] = None,
               description: Option[String] = None,
               main_image: Option[String] = None,
               secondary_image: Option[String] = None,
               autotag_hints: Option[String] = None) {

  def getId: String = id

  def getBSONId: String = _id.stringify

  def getName: String = name

  def getDisplayName: String = display_name

  def getParent: Option[BSONObjectID] = parent

  def getMainImage: String = main_image.orNull

  def getSecondaryImage: String = secondary_image.orNull

  // TODO All of this suggests we should be persisting autotags hints as a list
  def getAutotagHints: Option[String] = autotag_hints
  def autoTagHints: Seq[String] = {
    val commaSplitter = Splitter.on(",")
    val autotagHints = autotag_hints.map { autotagHints =>
      commaSplitter.split(autotagHints).asScala.map(_.trim).toSeq
    }.getOrElse(Seq.empty)
    autotagHints.filter(!Strings.isNullOrEmpty(_))
  }

  def isHidden: Boolean = hidden

  def getDescription: String = description.orNull

  def isFeatured: Boolean = featured

  def getPlace: Geocode = geocode.orNull

}
