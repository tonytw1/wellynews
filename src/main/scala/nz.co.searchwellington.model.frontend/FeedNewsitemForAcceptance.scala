package nz.co.searchwellington.model.frontend

import nz.co.searchwellington.model.Resource

case class FeedNewsitemForAcceptance(newsitem: FrontendNewsitem, localCopy: Option[Resource], suppressed: Boolean) {

  def getNewsitem: FrontendResource = newsitem

  def getLocalCopy: Resource = localCopy.orNull

  def isSupressed: Boolean = suppressed

}
