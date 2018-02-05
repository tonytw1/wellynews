package nz.co.searchwellington.model

import java.util.Date

import org.joda.time.DateTime

case class NewsitemImpl(override var id: Int = 0,
                        override var `type`: String = "",
                        override var title: String = "",
                        override var page: Option[String] = None,
                        override var http_status: Int = 0,
                        override var date: String = null,
                        override var description: Option[String] = None,
                        override var last_scanned: Option[String] = None,
                        override var last_changed: Option[String] = None,
                        override var live_time: Option[String] = None,
                        override var embargoed_until: Option[String] = None,
                        override var held: Int = 0,
                        override var url_words: Option[String] = None,
                        override var geocode: Option[Int] = None,
                        override var owner: Option[Int] = None,
                        override var publisher: Website = null,
                        var feed: Feed = null,
                        var commentFeed: CommentFeed = null,
                        var image: Image = null,
                        var accepted: Date = null,
                        var acceptedBy: User = null) extends PublishedResource with Newsitem {

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
