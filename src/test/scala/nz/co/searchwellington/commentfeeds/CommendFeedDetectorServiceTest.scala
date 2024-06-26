package nz.co.searchwellington.commentfeeds

import nz.co.searchwellington.commentfeeds.detectors.{CommentSlashFeedDetector, WellingtonScoopCommentFeedDetector}
import nz.co.searchwellington.model.Newsitem
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

import java.net.URL

class CommendFeedDetectorServiceTest {

  private val commentSlashFeedDetector = new CommentSlashFeedDetector()
  private val wellingtonScoopCommentFeedDetector = new WellingtonScoopCommentFeedDetector()
  private val source = Newsitem()

  private val service = {
    val availableDetectors = Seq(commentSlashFeedDetector, wellingtonScoopCommentFeedDetector).toArray // TODO would be nice to be able to autowire these somehow
    new CommentFeedDetectorService(availableDetectors)
  }

  @Test
  def shouldReturnIsCommentFeedIfAnyGuesserMatches(): Unit = {
    val scoopArticlesCommentFeedUrl = new URL("http://wellington.scoop.co.nz/?feed=rss2&p=130420")

    assertTrue(wellingtonScoopCommentFeedDetector.isValid(scoopArticlesCommentFeedUrl, source))
    assertTrue(service.isCommentFeedUrl(scoopArticlesCommentFeedUrl, source))
  }

  @Test
  def shouldDetectTheseExplicitExamplesAsCommentFeeds(): Unit = {
    assertTrue(service.isCommentFeedUrl(new URL("https://clubrugby.nz/wp/comments/feed/"), source))
  }

}
