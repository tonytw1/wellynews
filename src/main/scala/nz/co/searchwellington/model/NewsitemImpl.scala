package nz.co.searchwellington.model

import java.util.Date

import org.joda.time.format.ISODateTimeFormat

case class NewsitemImpl(override var id: Int = 0,
                        override var `type`: String = "",
                        override var title: Option[String] = None,
                        override var page: Option[String] = None,
                        override var http_status: Int = 0,
                        override var date2: Option[Date] = None,
                        override var description: Option[String] = None,
                        override var last_scanned2: Option[Date] = None,
                        override var last_changed2: Option[Date] = None,
                        override var live_time2: Option[Date] = None,
                        override var embargoed_until2: Option[Date] = None,
                        override var held2: Boolean = true,
                        override var url_words: Option[String] = None,
                        override var geocode: Option[Int] = None,
                        override var owner: Option[Int] = None,
                        override var publisher: Option[Int] = None,
                        var feed: Option[Int] = None,
                        var commentFeed: Option[Int] = None,
                        var image: Option[Int] = None,
                        var accepted: Option[String] = None,
                        var acceptedBy: Option[Int] = None) extends PublishedResource with Newsitem {

  override def getType = "N"

  //override def getComments: Seq[Comment] = {
    //if (getCommentFeed != null) return ImmutableList.builder[Comment].addAll(getCommentFeed.getComments).build
    //Collections.emptyList
   // Seq()
 // }

  def getCommentFeed: Option[Int] = commentFeed

  def setCommentFeed(commentFeed: Int): Unit = this.commentFeed = Some(commentFeed)

  override def getImage: Option[Int] = image

  override def setImage(image: Int): Unit = this.image = Some(image)

  override def getFeed: Option[Int] = feed

  override def setFeed(feed: Int): Unit = this.feed = Some(feed)

  override def getAccepted: Date = accepted.map(d => ISODateTimeFormat.dateParser().parseDateTime(d).toDate).getOrElse(null) // TODO

  override def setAccepted(accepted: Date): Unit = this.accepted = Some(accepted.toString)  // TODO

  override def getAcceptedBy: Option[Int] = acceptedBy

  override def setAcceptedBy(acceptedBy: Int): Unit = this.acceptedBy = Some(acceptedBy)

}
