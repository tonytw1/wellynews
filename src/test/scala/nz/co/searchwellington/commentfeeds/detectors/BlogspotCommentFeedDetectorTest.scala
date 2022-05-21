package nz.co.searchwellington.commentfeeds.detectors

import junit.framework.TestCase
import nz.co.searchwellington.model.Newsitem
import org.junit.Assert.{assertFalse, assertTrue}

import java.net.URL

class BlogspotCommentFeedDetectorTest extends TestCase {

  private val BLOGSPOT_COMMENT_URL = new URL("http://wellurban.blogspot.com/feeds/113750684886641660/comments/default")
  private val BLOGSPOT_ATOM_URL = new URL("http://wellurban.blogspot.com/atom.xml")
  private val source = new Newsitem()

  private val detector = new BlogspotCommentFeedDetector

  def testShouldDetectBlogspotCommentFeedUrl() = assertTrue(detector.isValid(BLOGSPOT_COMMENT_URL, source))

  def testShouldIgnoreBlogspotAtomFeed() = assertFalse(detector.isValid(BLOGSPOT_ATOM_URL, source))

}