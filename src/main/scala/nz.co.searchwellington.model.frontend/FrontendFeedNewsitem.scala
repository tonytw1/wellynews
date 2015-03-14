package nz.co.searchwellington.model.frontend

@SerialVersionUID(1L)
class FrontendFeedNewsitem extends FrontendNewsitem {
  private var isSuppressed: Boolean = false
  private var localCopy: Integer = null
  private var feed: FrontendFeed = null
  private var image: FrontendImage = null

  def this() {
    this()
    setType("FNI")
    this.isSuppressed = false
  }

  def getFeed: FrontendFeed = {
    return feed
  }

  def setFeed(feed: FrontendFeed) {
    this.feed = feed
  }

  override def getFrontendImage: FrontendImage = {
    return image
  }

  def getLocalCopy: Integer = {
    return localCopy
  }

  def setLocalCopy(localCopy: Integer) {
    this.localCopy = localCopy
  }

  def isSuppressed: Boolean = {
    return isSuppressed
  }

  def setSuppressed(isSuppressed: Boolean) {
    this.isSuppressed = isSuppressed
  }

  def setImage(image: FrontendImage) {
    this.image = image
  }
}