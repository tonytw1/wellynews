package nz.co.searchwellington.model.frontend

import nz.co.searchwellington.model.Resource

case class FeedNewsitemAcceptanceState(localCopy: Option[Resource], suppressed: Boolean) {
  def getLocalCopy: String = localCopy.map(_.id).orNull
  def isSuppressed: Boolean = suppressed
}
