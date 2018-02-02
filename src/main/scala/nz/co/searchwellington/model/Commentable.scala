package nz.co.searchwellington.model

trait Commentable {
  def getCommentFeed: CommentFeed

  def setCommentFeed(commentFeed: CommentFeed)

  def getComments: Seq[Comment]
}