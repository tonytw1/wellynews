package nz.co.searchwellington.model.frontend

case class FeedNewsitemAcceptanceState(val localCopy: Integer, val suppressed: Boolean) {

  def getLocalCopy: Integer = {
    localCopy
  }

  def isSuppressed: Boolean = {
    suppressed
  }

}
