package nz.co.searchwellington.model

import java.util.UUID

import reactivemongo.bson.BSONObjectID

case class Tag(_id: BSONObjectID = BSONObjectID.generate,
               id: String = UUID.randomUUID.toString,
               var name: String = "",
               var display_name: String = "",
               var parent: Option[BSONObjectID] = None,
               var hidden: Boolean = false,
               var featured: Boolean = false,
               var geocode: Option[Geocode] = None,
               var description: Option[String] = None,
               var main_image: Option[String] = None,
               var secondary_image: Option[String] = None,
               var related_twitter: Option[String] = None,
               var autotag_hints: Option[String] = None) {

  def getId: String = id

  def getName: String = name
  def setName(name: String): Unit = this.name = name

  def getDisplayName: String = display_name
  def setDisplayName(displayName: String): Unit = this.display_name = displayName

  def getParent: Option[BSONObjectID] = parent
  def setParent(parent: BSONObjectID): Unit = this.parent = Some(parent)

  def getMainImage: Option[String] = main_image
  def setMainImage(mainImage: String): Unit = this.main_image = Some(mainImage)

  def getSecondaryImage: Option[String] = secondary_image
  def setSecondaryImage(secondaryImage: String): Unit = this.secondary_image = Some(secondaryImage)

  def getRelatedTwitter: Option[String] = related_twitter
  def setRelatedTwitter(relatedTwitter: String): Unit = this.related_twitter = Some(relatedTwitter)

  def getAutotagHints: Option[String] = autotag_hints
  def setAutotagHints(autotag_hints: String): Unit = this.autotag_hints = Some(autotag_hints)

  def isHidden: Boolean = hidden
  def setHidden(hidden: Boolean): Unit = this.hidden == hidden

  def getDescription: Option[String] = description
  def setDescription(description: String): Unit = this.description = Some(description)

  def isFeatured: Boolean = featured
  def setFeatured(featured: Boolean): Unit = this.featured = featured

}
