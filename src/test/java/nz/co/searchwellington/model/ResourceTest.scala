package nz.co.searchwellington.model

import org.junit.Test
import org.junit.Assert.assertEquals

class ResourceTest {

  @Test
  def canEditCommonFieldsLikeTitle = {
    val website = Website(title = Some("Title"))

    val edited = website.copy(title = Some("New title"))

    assertEquals("New title", edited.title.get)
  }

  @Test
  def canTagResources = {
    val tag = Tag()
    val user = User()
    val website = Website(resource_tags = Seq(Tagging(tag_id = tag._id, user_id = user._id)))

    assertEquals(tag._id, website.resource_tags.head.tag_id)
  }

}
