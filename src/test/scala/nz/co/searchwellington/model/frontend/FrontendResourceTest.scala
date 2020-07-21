package nz.co.searchwellington.model.frontend

import java.util.UUID

import nz.co.searchwellington.model.Tag
import org.junit.Assert.assertEquals
import org.junit.Test

class FrontendResourceTest {

  @Test
  def resourcesWithNoManualOrAutomaticTaggingsShowAsNotTagged(): Unit  = {
    val newsitemWithNoTags = FrontendNewsitem(id = UUID.randomUUID().toString)

    assertEquals("Not tagged", newsitemWithNoTags.getTaggingStatus)
  }

  @Test
  def resourcesWithOnlyAutomaticTagsShowAsAutomaticallyTagged(): Unit  = {
    val aTag = Tag()

    val newsitemWithNoTags = FrontendNewsitem(id = UUID.randomUUID().toString,
      handTags = Seq.empty,
      tags = Seq(aTag)
    )

    assertEquals("Automatically tagged", newsitemWithNoTags.getTaggingStatus)
  }

  @Test
  def resourcesWithManualAndAutomaticTagsShowAManuallyTagged(): Unit  = {
    val aTag = Tag()
    val anotherTag = Tag()

    val newsitemWithNoTags = FrontendNewsitem(id = UUID.randomUUID().toString,
      handTags = Seq(aTag),
      tags = Seq(anotherTag)
    )

    assertEquals("Manually tagged", newsitemWithNoTags.getTaggingStatus)
  }

}
