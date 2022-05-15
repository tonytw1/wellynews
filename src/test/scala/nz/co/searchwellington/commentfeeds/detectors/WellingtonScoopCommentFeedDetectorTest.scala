package nz.co.searchwellington.commentfeeds.detectors

import org.junit.Assert.{assertFalse, assertTrue}
import org.junit.Test

import java.net.URL

class WellingtonScoopCommentFeedDetectorTest {

  private val SITE_FEED = urlOf("http://wellington.scoop.co.nz/?feed=rss2")
  private val COMMENT_FEED = urlOf("http://wellington.scoop.co.nz/?feed=rss2&p=34601")
  private val HTTPS_COMMENT_FEED = urlOf("https://wellington.scoop.co.nz/?feed=rss2&p=34601")

  private val detector = new WellingtonScoopCommentFeedDetector

  @Test def testShouldIgnoreSiteFeed(): Unit = assertFalse(detector.isValid(SITE_FEED))

  @Test def testShouldDetectCommentFeedUrl(): Unit = {
    assertTrue(detector.isValid(COMMENT_FEED))
    assertTrue(detector.isValid(HTTPS_COMMENT_FEED))
  }

  private def urlOf(url: String) = new URL(url)

}