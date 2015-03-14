package nz.co.searchwellington.model.frontend

class FrontendFeedNewsitem extends FrontendNewsitem {

  private var suppressed: Boolean = false
  private var localCopy: Integer = null
  private var feed: FrontendFeed = null
  private var image: FrontendImage = null

  def getFeed: FrontendFeed = {
    return feed
  }

  override def getFrontendImage: FrontendImage = {
    return image
  }

  def getLocalCopy: Integer = {
    return localCopy
  }

  def isSuppressed: Boolean = {
    this.suppressed
  }

}