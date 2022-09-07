package nz.co.searchwellington.linkchecking

import nz.co.searchwellington.model.Watchlist
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, verify, verifyNoMoreInteractions, when}

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

  @Test
  def shouldNotAlterResourcesIfContentHasNotChanged() {
    val agesAgo = DateTime.now.minusMonths(1)
    val checkResource = Watchlist(page = "http://localhost/a-page", last_changed = Some(agesAgo.toDate))
    when(snapshotArchive.getLatestFor(checkResource.page)).thenReturn(Some("Same old content"))
    val now = DateTime.now.withMillis(0)

    processor.process(checkResource, Some("Same old content"), now)

    assertEquals(Some(agesAgo.toDate), checkResource.last_changed)
    verify(snapshotArchive).getLatestFor(checkResource.page)
    verifyNoMoreInteractions(snapshotArchive)
  }

}
