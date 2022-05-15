package nz.co.searchwellington.commentfeeds.detectors

import org.junit.Assert.{assertFalse, assertTrue}
import org.junit.Test

import java.net.URL

class EyeOfTheFishCommentFeedDetectorTest {

  private val detector = new EyeOfTheFishCommentFeedDetector
  private val SITE_FEED = urlOf("http://eyeofthefish.org/feed/")
  private val COMMENT_FEED = urlOf("http://eyeofthefish.org/mayor-kerry-speaks/feed/")

  @Test def testShouldIgnoreSiteFeed(): Unit = assertFalse(detector.isValid(SITE_FEED))

  @Test def testShouldDetectCommentFeedUrl(): Unit = assertTrue(detector.isValid(COMMENT_FEED))

  private def urlOf(url: String) = new URL(url)

}