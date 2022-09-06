package nz.co.searchwellington.linkchecking

import nz.co.searchwellington.model.Watchlist
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, verify, when}

import scala.concurrent.ExecutionContext.Implicits.global

class ContentHasChangedProcessorTest {

  private val snapshotArchive = mock(classOf[InMemorySnapshotArchive])

  private val processor = new ContentHasChangedProcesser(snapshotArchive)

  @Test
  def shouldUpdateResourceLastChangedDateWhenChangeIsDetected() {
    val checkResource = Watchlist(page = "http://localhost/a-page")
    when(snapshotArchive.getLatestFor(checkResource.page)).thenReturn(Some("Some content"))
    val now = DateTime.now.withMillis(0)

    processor.process(checkResource, Some("Updated content"), now)

    verify(snapshotArchive).storeSnapshot(checkResource.page, "Updated content")
    assertEquals(Some(now.toDate), checkResource.last_changed)
  }

}
