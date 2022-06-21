package nz.co.searchwellington.commentfeeds.detectors

import nz.co.searchwellington.model.Newsitem
import org.junit.jupiter.api.Assertions.{assertFalse, assertTrue}
import org.junit.jupiter.api.Test

import java.net.URL

class BlogspotCommentFeedDetectorTest {

  private val BLOGSPOT_COMMENT_URL = new URL("http://wellurban.blogspot.com/feeds/113750684886641660/comments/default")
  private val BLOGSPOT_ATOM_URL = new URL("http://wellurban.blogspot.com/atom.xml")
  private val source = Newsitem()

  private val detector = new BlogspotCommentFeedDetector

  @Test
  def testShouldDetectBlogspotCommentFeedUrl(): Unit = assertTrue(detector.isValid(BLOGSPOT_COMMENT_URL, source))

  @Test
  def testShouldIgnoreBlogspotAtomFeed(): Unit = assertFalse(detector.isValid(BLOGSPOT_ATOM_URL, source))

}