package nz.co.searchwellington.model.frontend

import java.util.{Date, List}

import nz.co.searchwellington.model.{Comment, Twit}
import uk.co.eelpieconsulting.common.views.rss.RssFeedable

class FrontendNewsitem extends FrontendResource with RssFeedable {

  private var publisherName: String = null
  private var retweets: List[Twit] = null
  private var acceptedFromFeedName: String = null
  private var acceptedByProfilename: String = null
  private var comments: List[Comment] = null
  private var accepted: Date = null
  private var image: FrontendImage = null
  private var twitterMentions: List[FrontendTweet] = null

  def getPublisherName: String = {
    return publisherName
  }

  def setPublisherName(publisherName: String) {
    this.publisherName = publisherName
  }

  def getRetweets: List[Twit] = {
    return retweets
  }

  def setRetweets(retweets: List[Twit]) {
    this.retweets = retweets
  }

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

  def getTwitterMentions: List[FrontendTweet] = {
    return twitterMentions
  }

  def setTwitterMentions(twitterMentions: List[FrontendTweet]) {
    this.twitterMentions = twitterMentions
  }

  override def toString: String = {
    return "FrontendNewsitem [accepted=" + accepted + ", acceptedByProfilename=" + acceptedByProfilename + ", acceptedFromFeedName=" + acceptedFromFeedName + ", comments=" + comments + ", image=" + image + ", publisherName=" + publisherName + ", retweets=" + retweets + ", twitterMentions=" + twitterMentions + "]"
  }

}