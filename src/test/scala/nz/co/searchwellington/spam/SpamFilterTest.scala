package nz.co.searchwellington.spam

import java.util.UUID

import nz.co.searchwellington.model.Website
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class SpamFilterTest {

  private val filter = new SpamFilter

  @Test
  def shouldAllowNormalSubmission(): Unit = {
    val okResource = Website(id = UUID.randomUUID().toString, title = "Test site", page = "http://www.test.com.localhost", description = Some("test test"))

    assertFalse(filter.isSpam(okResource))
  }

  @Test
  def testShouldBlockRFIDUrl(): Unit = {
    val spamResource = Website(id = UUID.randomUUID().toString, title = "Test site", page = "http://www.rfid.com", description = Some("test test"))

    assertTrue(filter.isSpam(spamResource))
  }

  @Test
  def testShouldBlockByDescription(): Unit = {
    val spamResource = Website(id = UUID.randomUUID().toString, title = "Test site", page = "http://www.test.com.localhost", description = Some("test rfid test"))

    assertTrue(filter.isSpam(spamResource))
  }

}