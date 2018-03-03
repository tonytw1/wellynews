package nz.co.searchwellington.model.frontend

import java.util.{Date, List}

import nz.co.searchwellington.model.{Comment, Twit}
import uk.co.eelpieconsulting.common.views.rss.RssFeedable

class FrontendNewsitem extends FrontendResource with RssFeedable {

  private val acceptedFromFeedName: String = null
  private val acceptedByProfilename: String = null
  private val comments: List[Comment] = null
  private val accepted: Date = null
  private val image: FrontendImage = null

  def getAcceptedFromFeedName: String = {
    return acceptedFromFeedName
  }

  def getComments: List[Comment] = {
    return comments
  }

  def getAcceptedByProfilename: String = {
    return acceptedByProfilename
  }

  def getAccepted: Date = {
    return accepted
  }

  override def getAuthor: String = {
    return publisherName
  }

  def getFrontendImage: FrontendImage = {
    return image
  }

  override def getImageUrl: String = {
    return if (image != null) image.getUrl else null
  }

}
