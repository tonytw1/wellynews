package nz.co.searchwellington.spam

import junit.framework.TestCase
import nz.co.searchwellington.model.WebsiteImpl
import org.junit.Assert._
import org.junit.Test

class SpamFilterTest extends TestCase {
  private[spam] val filter: SpamFilter = new SpamFilter

  @Test
  @throws[Exception]
  def testAllowsNormalSubmission {
    val okResource = new WebsiteImpl
    okResource.setName("Test site")
    okResource.setUrl("http://www.test.com.localhost")
    okResource.setDescription("test test")
    assertFalse(filter.isSpam(okResource))
  }

  @Test
  @throws[Exception]
  def testShouldBlockRFID {
    val spamResource = new WebsiteImpl
    spamResource.setName("Test site")
    spamResource.setUrl("http://www.rfid.com")
    spamResource.setDescription("test test")
    assertTrue(filter.isSpam(spamResource))
  }

  @Test
  @throws[Exception]
  def testShouldBlockByDescription {
    val spamResource = new WebsiteImpl
    spamResource.setName("Test site")
    spamResource.setUrl("http://www.test.com.localhost")
    spamResource.setDescription("test rfid test")
    assertTrue(filter.isSpam(spamResource))
  }

}