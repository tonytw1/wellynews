package nz.co.searchwellington.model.frontend

import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

case class FeedNewsitemForAcceptance(val feedNewsitem: FeedItem, acceptanceState: FeedNewsitemAcceptanceState) {

  def getFeednewsitem: FeedItem = {
    feedNewsitem
  }

  def getAcceptanceState: FeedNewsitemAcceptanceState = {
    acceptanceState
  }

}