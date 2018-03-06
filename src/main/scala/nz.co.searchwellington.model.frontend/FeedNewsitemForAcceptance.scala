package nz.co.searchwellington.model.frontend

case class FeedNewsitemForAcceptance(val feedNewsitem: FrontendResource, acceptanceState: FeedNewsitemAcceptanceState) {

  def getFeednewsitem: FrontendResource = {
    feedNewsitem
  }

  def getAcceptanceState: FeedNewsitemAcceptanceState = {
    acceptanceState
  }

}
