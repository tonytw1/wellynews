package nz.co.searchwellington.model

import java.util.Date

trait Newsitem extends PublishedResource with Commentable {

  def getImage: Image
  def setImage(image: Image): Unit

  def getFeed: Feed
  def setFeed(feed: Feed): Unit

  def getAccepted: Date
  def setAccepted(accepted: Date): Unit

  def getAcceptedBy: User
  def setAcceptedBy(user: User): Unit
}
