package nz.co.searchwellington.model.frontend

class FeedNewsitemForAcceptance(val feedNewsitem: FrontendFeedNewsitem, acceptanceState: FeedNewsitemAcceptanceState) {

  def getFeednewsitem: FrontendFeedNewsitem = {
    return feedNewsitem
  }

  def getAcceptanceState: FeedNewsitemAcceptanceState = {
    return acceptanceState
  }

}