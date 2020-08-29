package nz.co.searchwellington.commentfeeds

import nz.co.searchwellington.commentfeeds.detectors.{CommentSlashFeedDetector, WellingtonScoopCommentFeedDetector}
import org.junit.Assert.assertTrue
import org.junit.Test

class CommendFeedDetectorServiceTest {

  val commentSlashFeedDetector = new CommentSlashFeedDetector()
  val wellingtonScoopCommentFeedDetector = new WellingtonScoopCommentFeedDetector()
  val service = {
    val availableDetectors = Seq(commentSlashFeedDetector, wellingtonScoopCommentFeedDetector).toArray // TODO would be nice to be able to autowire these somehow
    new CommentFeedDetectorService(availableDetectors)
  }

  @Test
  def shouldReturnIsCommentFeedIfAnyGuesserMatches(): Unit = {
    val scoopArticlesCommentFeedUrl = "http://wellington.scoop.co.nz/?feed=rss2&p=130420"

    assertTrue(wellingtonScoopCommentFeedDetector.isValid(scoopArticlesCommentFeedUrl))
    assertTrue(service.isCommentFeedUrl(scoopArticlesCommentFeedUrl))
  }

  @Test
  def shouldDetectTheseExplicitExamplesAsCommentFeeds(): Unit = {
    assertTrue(service.isCommentFeedUrl("https://clubrugby.nz/wp/comments/feed/"))
  }

}
