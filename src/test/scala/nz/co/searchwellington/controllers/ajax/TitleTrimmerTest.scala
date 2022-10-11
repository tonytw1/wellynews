package nz.co.searchwellington.controllers.ajax

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TitleTrimmerTest {

  @Test
  def shouldTrimTrailingPublisherNameFromTitle(): Unit = {
    val trimmer = new TitleTrimmer()

    val trimmed = trimmer.trimTitleSuffix("Stu Jacobs joins Upper Hutt City Football - Upper Hutt City Football", "Upper Hutt City Football")
    assertEquals("Stu Jacobs joins Upper Hutt City Football", trimmed)

    val trimmedMdash = trimmer.trimTitleSuffix("Lōemis Art Show — Lōemis", "Lōemis")
    assertEquals("Lōemis Art Show", trimmedMdash)

    val trimmedNdash = trimmer.trimTitleSuffix("THE WASP – Circa Theatre", "Circa Theatre")
    assertEquals("THE WASP", trimmedNdash)
  }

}
