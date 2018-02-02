package nz.co.searchwellington.model

import java.util.Date

import org.joda.time.DateTime

case class NewsitemImpl(override var id: Int = 0,
                        override var `type`: String = "",
                        override var name: String = "",
                        override var url: String = "",
                        override var httpStatus: Int = 0,
                        override var date: Date = DateTime.now.toDate,
                        override var description: String = "",
                        override var lastScanned: Date = null,
                        override var lastChanged: Date = null,
                        override var liveTime: Date = null,
                        override var embargoedUntil: Date = null,
                        override var held: Boolean = false,
                        override var urlWords: String = null,
                        override var geocode: Geocode = null,
                        override var owner: User = null,
                        override var publisher: Website = null,
                        var feed: Feed = null,
                        var commentFeed: CommentFeed = null,
                        var image: Image = null,
                        var accepted: Date = null,
                        var acceptedBy: User = null) extends PublishedResourceImpl with Newsitem {

  override def getType = "N"

  override def getComments: Seq[Comment] = {
    //if (getCommentFeed != null) return ImmutableList.builder[Comment].addAll(getCommentFeed.getComments).build
    //Collections.emptyList
    Seq()
  }

  override def getCommentFeed: CommentFeed = commentFeed

  override def setCommentFeed(commentFeed: CommentFeed): Unit = this.commentFeed = commentFeed

  override def getImage: Image = image

  override def setImage(image: Image): Unit = this.image = image

  override def getFeed: Feed = feed

  override def setFeed(feed: Feed): Unit = this.feed = feed

  override def getAccepted: Date = accepted

  override def setAccepted(accepted: Date): Unit = this.accepted = accepted

  override def getAcceptedBy: User = acceptedBy

  override def setAcceptedBy(acceptedBy: User): Unit = this.acceptedBy = acceptedBy

}
