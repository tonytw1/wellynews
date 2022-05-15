package nz.co.searchwellington.commentfeeds.detectors

import nz.co.searchwellington.model.Newsitem
import org.junit.Assert.{assertFalse, assertTrue}
import org.junit.Test

import java.net.URL

class ExistingNewsitemCommentFeedDetectorTest {

  private val existing = new ExistingNewsitemCommentFeedDetector()

  @Test
  def shouldDetectSlashFeedSuffixOfSourceNewsitemAsCommentFeed(): Unit = {
    val source = Newsitem(page = "https://wtmc.org.nz/trip-report/st-arnaud-range-and-the-camel/")

    assertTrue(existing.isValid(new URL("https://wtmc.org.nz/trip-report/st-arnaud-range-and-the-camel/feed/"), source))
    assertTrue(existing.isValid(new URL("https://eyeofthefish.org/te-matapihi/feed/"), Newsitem(page = "https://eyeofthefish.org/te-matapihi/")))
  }

  @Test
  def shouldNotObjectToSlashFeedsWhichAreNotForTheSourceNewsitem(): Unit = {
    val source = Newsitem(page = "https://wtmc.org.nz/trip-report/st-arnaud-range-and-the-camel/")

    assertFalse(existing.isValid(new URL("https://wtmc.org.nz/feed/"), source))
  }

}
