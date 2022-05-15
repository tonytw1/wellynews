package nz.co.searchwellington.commentfeeds.detectors

import nz.co.searchwellington.model.Newsitem
import org.junit.Assert.{assertFalse, assertTrue}
import org.junit.Test

import java.net.URL

class DateRegexCommentFeedDetectorTest {

  private val commentFeedDetector = new DateRegexCommentFeedDetector

  @Test
  def shouldMatchUrlWithDateInThemAsTheseAreAlmostAlwaysCommentFeedsForSpecficPost(): Unit = {
    val source = new Newsitem()
    val url = new URL("http://www.blah.nz/something/2011/01/20/comments")
    assertTrue(commentFeedDetector.isValid(url, source))
  }

  @Test
  def shouldNotGiveObviousFalsePositives(): Unit = {
    val source = new Newsitem()
    val url = new URL("http://www.blah.nz/something/comments")
    assertFalse(commentFeedDetector.isValid(url, source))
  }

}