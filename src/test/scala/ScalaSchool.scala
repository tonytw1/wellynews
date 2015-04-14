import nz.co.searchwellington.model.NewsitemImpl
import org.junit.Assert._
import org.junit.Test

class ScalaSchool {

  @Test def youAreAllowedToReturnEarlyButNeedToUseAnExplictReturn {
    assertEquals(1, earlyReturn)
    assertEquals(2, earlyReturnWithoutExplictReturn)
  }

  def earlyReturn: Int = {
    if (true) {
      return 1
    }
    2
  }

  def earlyReturnWithoutExplictReturn: Int = {
    if (true) {
      1
    }
    2
  }

}
