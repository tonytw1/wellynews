package nz.co.searchwellington.model

trait Commentable {
  def getCommentFeed: Option[Int]

  def setCommentFeed(commentFeed: Int)

  // def getComments: Seq[Comment]
}