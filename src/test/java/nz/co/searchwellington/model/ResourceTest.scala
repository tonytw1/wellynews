package nz.co.searchwellington.model

import org.junit.Test
import org.junit.Assert.assertEquals

class ResourceTest {

  @Test
  def canEditCommonFieldsLikeTitle(): Unit = {
    val website = Website(title = "Title")

    val edited = website.copy(title = "New title")

    assertEquals("New title", edited.title)
  }

  @Test
  def canTagResources(): Unit = {
    val tag = Tag()
    val user = User()
    val website = Website(title = "A website", resource_tags = Seq(Tagging(tag_id = tag._id, user_id = user._id)))

    assertEquals(tag._id, website.resource_tags.head.tag_id)
  }

  @Test
  def canEditResourceTags(): Unit = {
    val tag = Tag()
    val user = User()
    val website = Website(title = "A website", resource_tags = Seq(Tagging(tag_id = tag._id, user_id = user._id)))

    val anotherTag = Tag()
    val edited = website.copy(resource_tags = Seq(Tagging(tag_id = anotherTag._id, user_id = user._id)))

    assertEquals(anotherTag._id, edited.resource_tags.head.tag_id)
  }

}
