package nz.co.searchwellington.commentfeeds

import nz.co.searchwellington.commentfeeds.detectors.WellingtonScoopCommentFeedDetector
import org.junit.Assert.assertTrue
import org.junit.Test

class CommendFeedDetectorServiceTest {

  @Test
  def shouldReturnIsCommentFeedIfAnyGuesserMatches(): Unit = {
    val scoopArticlesCommentFeedUrl = "http://wellington.scoop.co.nz/?feed=rss2&p=130420"
    val detector = new WellingtonScoopCommentFeedDetector()

    val service = new CommentFeedDetectorService(Seq(detector).toArray)

    assertTrue(detector.isValid(scoopArticlesCommentFeedUrl))
    assertTrue(service.isCommentFeedUrl(scoopArticlesCommentFeedUrl))
  }

}
