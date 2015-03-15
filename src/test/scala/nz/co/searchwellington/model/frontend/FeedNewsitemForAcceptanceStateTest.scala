package nz.co.searchwellington.model.frontend

import org.junit.Assert._
import org.junit.Test

class FeedNewsitemForAcceptanceStateTest {

  @throws(classOf[Exception])
  @Test def constructorSetsFieldValues: Unit = {
    val a = new FeedNewsitemAcceptanceState(123, true);
    
    assertEquals(123, a.getLocalCopy)
    assertTrue(a.isSuppressed)
  }

}
