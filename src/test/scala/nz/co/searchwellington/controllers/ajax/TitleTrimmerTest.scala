package nz.co.searchwellington.controllers.ajax

import nz.co.searchwellington.model.Website
import org.junit.Assert.assertEquals
import org.junit.Test

class TitleTrimmerTest {

  @Test
  def shouldTrimTrailingPublisherNameFromTitle(): Unit = {
    val publisher = new Website(title = Some("Upper Hutt City Football"))
    val pageTitle = "Stu Jacobs joins Upper Hutt City Football - Upper Hutt City Football"

    val trimmed = new TitleTrimmer().trimTitle(pageTitle, Some(publisher))

    assertEquals("Stu Jacobs joins Upper Hutt City Football", trimmed)
  }

}
