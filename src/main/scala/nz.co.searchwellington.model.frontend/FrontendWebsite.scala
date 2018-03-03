package nz.co.searchwellington.model.frontend

@SerialVersionUID(1L)
class FrontendWebsite extends FrontendResource {
  private var urlWords: String = null

  override def getUrlWords: String = {
    return urlWords
  }

  override def toString: String = {
    return "FrontendWebsite [urlWords=" + urlWords + ", getUrlWords()=" + getUrlWords + ", getAuthor()=" + getAuthor + ", getDate()=" + getDate + ", getDescription()=" + getDescription + ", getHandTags()=" + getHandTags + ", getHeadline()=" + getHeadline + ", getHttpStatus()=" + getHttpStatus + ", getId()=" + getId + ", getImageUrl()=" + getImageUrl + ", getLatLong()=" + getLatLong + ", getLiveTime()=" + getLiveTime + ", getLocation()=" + getLocation + ", getName()=" + getName + ", getOwner()=" + getOwner + ", getPlace()=" + getPlace + ", getTags()=" + getTags + ", getType()=" + getType + ", getUrl()=" + getUrl + ", getWebUrl()=" + getWebUrl + ", isHeld()=" + isHeld + "]"
  }
}