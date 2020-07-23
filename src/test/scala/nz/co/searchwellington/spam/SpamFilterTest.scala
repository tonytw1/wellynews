package nz.co.searchwellington.spam

import java.util.UUID

import junit.framework.TestCase
import nz.co.searchwellington.model.Website
import org.junit.Assert._
import org.junit.Test

class SpamFilterTest extends TestCase {

  private val filter = new SpamFilter

  @Test
  def testAllowsNormalSubmission {
    val okResource = Website(id = UUID.randomUUID().toString, title = Some("Test site"), page = "http://www.test.com.localhost", description = Some("test test"))

    assertFalse(filter.isSpam(okResource))
  }

  @Test
  def testShouldBlockRFID {
    val spamResource = Website(id = UUID.randomUUID().toString, title = Some("Test site"), page = "http://www.rfid.com", description = Some("test test"))

    assertTrue(filter.isSpam(spamResource))
  }

  @Test
  def testShouldBlockByDescription {
    val spamResource = Website(id = UUID.randomUUID().toString, title = Some("Test site"), page = "http://www.test.com.localhost", description = Some("test rfid test"))

    assertTrue(filter.isSpam(spamResource))
  }

}