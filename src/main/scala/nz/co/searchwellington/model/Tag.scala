package nz.co.searchwellington.model

case class Tag(id: Int = 0,
               var name: String = "",
               var display_name: String = "",
               var parent: Option[Int] = None,
               var flickrCount: Option[Int] = None,
               var hidden: Int = 0,
               var featured: Int = 0,
               var geocode_id: Option[Int] = None,
               var description: Option[String] = None,
               var main_image: Option[String] = None,
               var secondary_image: Option[String] = None,
               var related_feed: Option[Long] = None,
               var related_twitter: Option[String] = None,
               var autotag_hints: Option[String] = None) {

  def getId: Int = id

  def getName: String = name
  def setName(name: String): Unit = this.name = name

  def getDisplayName: String = display_name
  def setDisplayName(displayName: String): Unit = this.display_name = displayName

  def getParent: Option[Int] = parent
  def setParent(parent: Int): Unit = this.parent = Some(parent)

  def getMainImage: Option[String] = main_image
  def setMainImage(mainImage: String): Unit = this.main_image = Some(mainImage)

  def getSecondaryImage: Option[String] = secondary_image
  def setSecondaryImage(secondaryImage: String): Unit = this.secondary_image = Some(secondaryImage)

  def getRelatedFeed: Option[Long] = this.related_feed
  def setRelatedFeed(relatedFeed: Int): Unit = this.related_feed = Some(relatedFeed)

  def getRelatedTwitter: Option[String] = related_twitter
  def setRelatedTwitter(relatedTwitter: String): Unit = this.related_twitter = Some(relatedTwitter)

  def getAutotagHints: Option[String] = autotag_hints
  def setAutotagHints(autotag_hints: String): Unit = this.autotag_hints = Some(autotag_hints)

  def isHidden: Boolean = hidden == 1
  def setHidden(hidden: Boolean): Unit = this.hidden == (if (hidden) 1 else 0)

  def getGeocode: Option[Int] = geocode_id
  def setGeocode(geocode: Int): Unit = this.geocode_id = Some(geocode)

  def getDescription: Option[String] = description
  def setDescription(description: String): Unit = this.description = Some(description)

  def isFeatured: Boolean = featured == 1
  def setFeatured(featured: Boolean): Unit = this.featured = (if (featured) 1 else 0)

}
