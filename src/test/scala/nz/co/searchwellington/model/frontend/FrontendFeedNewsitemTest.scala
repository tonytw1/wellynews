package nz.co.searchwellington.model.frontend

import org.junit.Assert._
import org.junit.{Before, Test}
import org.mockito.{Mock, MockitoAnnotations}
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
