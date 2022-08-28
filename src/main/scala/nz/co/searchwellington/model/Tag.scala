package nz.co.searchwellington.model

import nz.co.searchwellington.model.geo.Geocode
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
               hints: Seq[String] = Seq.empty) extends StringWrangling {

  def getId: String = id

  def getBSONId: String = _id.stringify

  def getName: String = name

  def getDisplayName: String = display_name

  def getParent: Option[BSONObjectID] = parent

  def getMainImage: String = main_image.orNull

  def getSecondaryImage: String = secondary_image.orNull

  def isHidden: Boolean = hidden

  def getDescription: String = description.orNull

  def isFeatured: Boolean = featured

  def getGeocode: Geocode = geocode.orNull

}
