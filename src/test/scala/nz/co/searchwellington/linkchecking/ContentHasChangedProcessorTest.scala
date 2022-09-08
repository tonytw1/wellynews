package nz.co.searchwellington.linkchecking

import nz.co.searchwellington.model.Watchlist
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.{assertEquals, fail}
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, verify, verifyNoMoreInteractions, when}

import scala.concurrent.ExecutionContext.Implicits.global

class ContentHasChangedProcessorTest {

  private val snapshotArchive = mock(classOf[SnapshotArchive])
  private val pageContentHasher = mock(classOf[PageContentHasher])

  private val processor = new ContentHasChangedProcessor(snapshotArchive, pageContentHasher)

  private val agesAgo = DateTime.now.minusMonths(1)

  @Test
  def shouldUpdateResourceLastChangedDateAndLatestHashWhenChangeIsDetected() {
    val checkResource = Watchlist(page = "http://localhost/a-page")
    when(snapshotArchive.getLatestHashFor(checkResource.page)).thenReturn(Some("SOME_CONTENT_HASH"))
    when(pageContentHasher.hashPageContent("Updated content")).thenReturn("UPDATED_CONTENT_HASH")
    val now = DateTime.now

    processor.process(checkResource, Some("Updated content"), now)

    verify(snapshotArchive).storeHash(checkResource.page, "UPDATED_CONTENT_HASH")
    assertEquals(Some(now.toDate), checkResource.last_changed)
  }

  @Test
  def shouldNotAlterResourcesIfContentHasNotChanged() {
    val checkResource = Watchlist(page = "http://localhost/a-page", last_changed = Some(agesAgo.toDate))
    when(snapshotArchive.getLatestHashFor(checkResource.page)).thenReturn(Some("SAME_OLD_CONTENT_HASH"))
    when(pageContentHasher.hashPageContent("Same old content")).thenReturn("SAME_OLD_CONTENT_HASH")
    val now = DateTime.now

    processor.process(checkResource, Some("Same old content"), now)

    assertEquals(Some(agesAgo.toDate), checkResource.last_changed)
    verify(snapshotArchive).getLatestHashFor(checkResource.page)
    verifyNoMoreInteractions(snapshotArchive)
  }

  @Test
  def notHavingAPreviousSnapshotToCompareDoesNotMeanContentHasChanged(): Unit = {
    val checkResource = Watchlist(page = "http://localhost/a-page", last_changed = Some(agesAgo.toDate))
    when(snapshotArchive.getLatestHashFor(checkResource.page)).thenReturn(None)

    processor.process(checkResource, Some("Same old content"), DateTime.now)

    assertEquals(Some(agesAgo.toDate), checkResource.last_changed)
    verify(snapshotArchive).getLatestHashFor(checkResource.page)
    verifyNoMoreInteractions(snapshotArchive)
  }

}
