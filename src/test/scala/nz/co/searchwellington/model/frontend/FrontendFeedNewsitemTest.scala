package nz.co.searchwellington.model.frontend

import nz.co.searchwellington.model.TagBuilder
import nz.co.searchwellington.tagging.PlaceAutoTagger
import org.junit.Assert._
import org.junit.{Before, Test}
import org.mockito.{MockitoAnnotations, Mock}
import org.mockito.Mockito._
import uk.co.eelpieconsulting.common.geo.model.Place

class FrontendFeedNewsitemTest {

  @Mock var place: Place = null;

  @Before def setUp {
    MockitoAnnotations.initMocks(this)
  }

  @Test def constructorSetsFieldValues: Unit = {
    val f = new FrontendFeedNewsitem("Test", "http://localhost", null, null, place, null, null, null)

    assertEquals("Test", f.getName)
    assertEquals("http://localhost", f.getUrl)
    assertNotNull(f.getPlace)
  }

}
