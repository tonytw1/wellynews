package nz.co.searchwellington.commentfeeds.detectors

import nz.co.searchwellington.model.Newsitem
import org.junit.Assert.{assertFalse, assertTrue}
import org.junit.Test

import java.net.URL

class EyeOfTheFishCommentFeedDetectorTest {

  private val detector = new EyeOfTheFishCommentFeedDetector
  private val SITE_FEED = new URL("http://eyeofthefish.org/feed/")
  private val COMMENT_FEED = new URL("http://eyeofthefish.org/mayor-kerry-speaks/feed/")
  private val source = Newsitem(page = "http://eyeofthefish.org/mayor-kerry-speaks")

  @Test def testShouldIgnoreSiteFeed(): Unit = assertFalse(detector.isValid(SITE_FEED, source))

  @Test def testShouldDetectCommentFeedUrl(): Unit = assertTrue(detector.isValid(COMMENT_FEED, source))

}