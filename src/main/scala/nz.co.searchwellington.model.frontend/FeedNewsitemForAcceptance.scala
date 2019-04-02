package nz.co.searchwellington.model.frontend

case class FeedNewsitemForAcceptance(feedNewsitem: FrontendResource, acceptanceState: FeedNewsitemAcceptanceState) {

  def getFeednewsitem: FrontendResource = {
    feedNewsitem
  }

  def getAcceptanceState: FeedNewsitemAcceptanceState = {
    acceptanceState
  }

}
