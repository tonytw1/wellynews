package nz.co.searchwellington.spam

import junit.framework.TestCase
import nz.co.searchwellington.model.Website
import org.junit.Assert._
import org.junit.Test

class SpamFilterTest extends TestCase {
  private[spam] val filter: SpamFilter = new SpamFilter

  @Test
  @throws[Exception]
  def testAllowsNormalSubmission {
    val okResource = Website(title = Some("Test site"), page = Some("http://www.test.com.localhost"), description = Some("test test"))

    assertFalse(filter.isSpam(okResource))
  }

  @Test
  @throws[Exception]
  def testShouldBlockRFID {
    val spamResource = Website(title = Some("Test site"), page = Some("http://www.rfid.com"), description = Some("test test"))

    assertTrue(filter.isSpam(spamResource))
  }

  @Test
  @throws[Exception]
  def testShouldBlockByDescription {
    val spamResource = Website(title = Some("Test site"), page = Some("http://www.test.com.localhost"), description = Some("test rfid test"))

    assertTrue(filter.isSpam(spamResource))
  }

}