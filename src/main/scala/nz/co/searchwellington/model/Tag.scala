package nz.co.searchwellington.model

import java.util.UUID

import reactivemongo.bson.BSONObjectID

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

  def getName: String = name

  def getDisplayName: String = display_name

  def getParent: Option[BSONObjectID] = parent

  def getMainImage: Option[String] = main_image

  def getSecondaryImage: Option[String] = secondary_image

  def getAutotagHints: Option[String] = autotag_hints

  def isHidden: Boolean = hidden

  def getDescription: String = description.orNull

  def isFeatured: Boolean = featured

  def getPlace: Geocode = geocode.orNull


}
