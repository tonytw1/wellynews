package nz.co.searchwellington.model

import org.junit.Test
import org.junit.Assert.assertEquals

class ResourceTest {

  @Test
  def canEditCommonFieldsLikeTitle: Unit = {
    val website = Website(title = Some("Title"))

    val edited = website.copy(title = Some("New title"))

    assertEquals("New title", edited.title.get)
  }

}
