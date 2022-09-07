package nz.co.searchwellington.linkchecking

import nz.co.searchwellington.model.Watchlist
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.{assertEquals, fail}
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, verify, verifyNoMoreInteractions, when}

import scala.concurrent.ExecutionContext.Implicits.global

class ContentHasChangedProcessorTest {

  private val snapshotArchive = mock(classOf[SnapshotArchive])

  private val processor = new ContentHasChangedProcessor(snapshotArchive)

  private val agesAgo = DateTime.now.minusMonths(1)

  @Test
  def shouldUpdateResourceLastChangedDateWhenChangeIsDetected() {
    val checkResource = Watchlist(page = "http://localhost/a-page")
    when(snapshotArchive.getLatestFor(checkResource.page)).thenReturn(Some("Some content"))
    val now = DateTime.now

    processor.process(checkResource, Some("Updated content"), now)

    verify(snapshotArchive).storeSnapshot(checkResource.page, "Updated content")
    assertEquals(Some(now.toDate), checkResource.last_changed)
  }

  @Test
  def shouldNotAlterResourcesIfContentHasNotChanged() {
    val checkResource = Watchlist(page = "http://localhost/a-page", last_changed = Some(agesAgo.toDate))
    when(snapshotArchive.getLatestFor(checkResource.page)).thenReturn(Some("Same old content"))
    val now = DateTime.now

    processor.process(checkResource, Some("Same old content"), now)

    assertEquals(Some(agesAgo.toDate), checkResource.last_changed)
    verify(snapshotArchive).getLatestFor(checkResource.page)
    verifyNoMoreInteractions(snapshotArchive)
  }

  @Test
  def notHavingAPreviousSnapshotToCompareDoesNotMeanContentHasChanged(): Unit = {
    val checkResource = Watchlist(page = "http://localhost/a-page", last_changed = Some(agesAgo.toDate))
    when(snapshotArchive.getLatestFor(checkResource.page)).thenReturn(None)

    processor.process(checkResource, Some("Same old content"), DateTime.now)

    assertEquals(Some(agesAgo.toDate), checkResource.last_changed)
    verify(snapshotArchive).getLatestFor(checkResource.page)
    verifyNoMoreInteractions(snapshotArchive)
  }

}
