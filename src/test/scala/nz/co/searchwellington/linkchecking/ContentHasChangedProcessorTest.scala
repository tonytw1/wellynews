package nz.co.searchwellington.linkchecking

import nz.co.searchwellington.model.Watchlist
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

import scala.concurrent.ExecutionContext.Implicits.global

class ContentHasChangedProcessorTest {

  private val snapshotArchive = mock(classOf[InMemorySnapshotArchive])

  private val processor = new ContentHasChangedProcesser(snapshotArchive)

  @Test
  def shouldUpdateResourceLastChangedDateWhenChangeIsDetected() {
    val checkResource = Watchlist()

    processor.process(checkResource, Some("SOME CONTENT"), DateTime.now)

    // TODO assert that
    // records snapshot for the current content
    // updates the resource last change is a change is detected
    fail()
  }

}
