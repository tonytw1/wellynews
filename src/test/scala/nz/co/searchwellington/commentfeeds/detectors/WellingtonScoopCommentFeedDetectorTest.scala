package nz.co.searchwellington.commentfeeds.detectors

import nz.co.searchwellington.model.Newsitem
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

import java.net.URL

class WellingtonScoopCommentFeedDetectorTest {

  private val SITE_FEED = new URL("http://wellington.scoop.co.nz/?feed=rss2")
  private val COMMENT_FEED = new URL("http://wellington.scoop.co.nz/?feed=rss2&p=34601")
  private val HTTPS_COMMENT_FEED = new URL("https://wellington.scoop.co.nz/?feed=rss2&p=34601")
  private val source = Newsitem()

  private val detector = new WellingtonScoopCommentFeedDetector

  @Test def testShouldIgnoreSiteFeed(): Unit = assertFalse(detector.isValid(SITE_FEED, source))

  @Test def testShouldDetectCommentFeedUrl(): Unit = {
    assertTrue(detector.isValid(COMMENT_FEED, source))
    assertTrue(detector.isValid(HTTPS_COMMENT_FEED, source))
  }

}