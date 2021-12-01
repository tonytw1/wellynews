package nz.co.searchwellington.model

import nz.co.searchwellington.utils.StringWrangling
import reactivemongo.api.bson.BSONObjectID

import java.util.UUID

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
               autotag_hints: Option[String] = None,
               hints: Seq[String] = Seq.empty) extends StringWrangling {

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
    autotag_hints.map(splitCommaDelimited).getOrElse(Seq.empty)
  }

  def isHidden: Boolean = hidden

  def getDescription: String = description.orNull

  def isFeatured: Boolean = featured

  def getPlace: Geocode = geocode.orNull

}
