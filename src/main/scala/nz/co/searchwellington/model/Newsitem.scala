package nz.co.searchwellington.model

import java.util.Date

trait Newsitem extends PublishedResource with Commentable {

  def getImage: Option[Int]
  def setImage(image: Int): Unit

  def getFeed: Option[Int]
  def setFeed(feed: Int): Unit

  def getAccepted: Date
  def setAccepted(accepted: Date): Unit

  def getAcceptedBy: Option[Int]
  def setAcceptedBy(user: Int): Unit
}
