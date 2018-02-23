package nz.co.searchwellington.model

import java.util.Date

case class Newsitem(override var id: Int = 0,
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
                    override var publisher: Option[Long] = None,
                    var feed: Option[Int] = None,
                    var commentFeed: Option[Int] = None,
                    var image: Option[Int] = None,
                    var accepted2: Option[Date] = None,
                    var acceptedBy: Option[Int] = None) extends PublishedResource with Commentable {

  override def getType = "N"

  //override def getComments: Seq[Comment] = {
  //if (getCommentFeed != null) return ImmutableList.builder[Comment].addAll(getCommentFeed.getComments).build
  //Collections.emptyList
  // Seq()
  // }

  def getCommentFeed: Option[Int] = commentFeed

  def setCommentFeed(commentFeed: Int): Unit = this.commentFeed = Some(commentFeed)

  def getImage: Option[Int] = image

  def setImage(image: Int): Unit = this.image = Some(image)

  def getFeed: Option[Int] = feed

  def setFeed(feed: Int): Unit = this.feed = Some(feed)

  def getAccepted: Date = accepted2.getOrElse(null) // TODO

  def setAccepted(accepted: Date): Unit = this.accepted2 = Some(accepted)

  def getAcceptedBy: Option[Int] = acceptedBy

  def setAcceptedBy(acceptedBy: Int): Unit = this.acceptedBy = Some(acceptedBy)

}

