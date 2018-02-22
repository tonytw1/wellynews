package nz.co.searchwellington.model.frontend

import java.util.{Date, List}

import nz.co.searchwellington.model.{Comment, Twit}
import uk.co.eelpieconsulting.common.views.rss.RssFeedable

class FrontendNewsitem extends FrontendResource with RssFeedable {

  private var acceptedFromFeedName: String = null
  private var acceptedByProfilename: String = null
  private var comments: List[Comment] = null
  private var accepted: Date = null
  private var image: FrontendImage = null

  def getAcceptedFromFeedName: String = {
    return acceptedFromFeedName
  }

  def setAcceptedFromFeedName(acceptedFromFeedName: String) {
    this.acceptedFromFeedName = acceptedFromFeedName
  }

  def getComments: List[Comment] = {
    return comments
  }

  def setComments(comments: List[Comment]) {
    this.comments = comments
  }

  def getAcceptedByProfilename: String = {
    return acceptedByProfilename
  }

  def setAcceptedByProfilename(acceptedByProfilename: String) {
    this.acceptedByProfilename = acceptedByProfilename
  }

  def getAccepted: Date = {
    return accepted
  }

  def setAccepted(accepted: Date) {
    this.accepted = accepted
  }

  override def getAuthor: String = {
    return publisherName
  }

  def getFrontendImage: FrontendImage = {
    return image
  }

  def setFrontendImage(image: FrontendImage) {
    this.image = image
  }

  override def getImageUrl: String = {
    return if (image != null) image.getUrl else null
  }

}
