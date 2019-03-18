package nz.co.searchwellington.model.frontend

case class FeedNewsitemAcceptanceState(localCopy: String, suppressed: Boolean) {
  def getLocalCopy: String = localCopy
  def isSuppressed: Boolean = suppressed
}
