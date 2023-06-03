package nz.co.searchwellington.linkchecking

import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PageContentHasherTest {

  private val hasher = new PageContentHasher

  @Test
  def shouldReturnSameHashForPagesWithSameContentEvenIfTheNonUserVisibleHTMLeDiffers(): Unit = {
    val before = loadTestFile("urbandreambrokerage_1.html")
    val after = loadTestFile("urbandreambrokerage_2.html")

    assertEquals(hasher.hashPageContent(before), hasher.hashPageContent(after))
  }

  private def loadTestFile(filename: String): String = {
    IOUtils.toString(this.getClass.getClassLoader.getResourceAsStream(filename),
      java.nio.charset.StandardCharsets.UTF_8
    )
  }

}
