package nz.co.searchwellington.model.frontend

import nz.co.searchwellington.model.Tag
import org.junit.Assert.assertEquals
import org.junit.Test

import java.util.UUID
import scala.jdk.CollectionConverters._

class FrontendResourceTest {

  @Test
  def resourcesWithNoManualOrAutomaticTaggingsShowAsNotTagged(): Unit = {
    val newsitemWithNoTags = FrontendNewsitem(id = UUID.randomUUID().toString)

    assertEquals("Not tagged", newsitemWithNoTags.getTaggingStatus)

    assertEquals(Seq.empty.asJava, newsitemWithNoTags.getTaggingsToShow)
  }

  @Test
  def resourcesWithOnlyAutomaticTagsShowAsAutomaticallyTagged(): Unit = {
    val aTag = Tag()

    val newsitemWithAutomaticTagsOnly = FrontendNewsitem(id = UUID.randomUUID().toString,
      handTags = Some(Seq.empty),
      tags = Some(Seq(aTag))
    )

    assertEquals("Automatically tagged as: ", newsitemWithAutomaticTagsOnly.getTaggingStatus)
    assertEquals(Seq(aTag).asJava, newsitemWithAutomaticTagsOnly.getTaggingsToShow)
  }

  @Test
  def resourcesWithManualAndAutomaticTagsShowAManuallyTagged(): Unit = {
    val aTag = Tag()
    val anotherTag = Tag()

    val newsitemWithManualAndAutomaticTags = FrontendNewsitem(id = UUID.randomUUID().toString,
      handTags = Some(Seq(aTag)),
      tags = Some(Seq(anotherTag))
    )

    assertEquals("Tagged as: ", newsitemWithManualAndAutomaticTags.getTaggingStatus)
    assertEquals(Seq(aTag).asJava, newsitemWithManualAndAutomaticTags.getTaggingsToShow)
  }

  @Test
  def shouldFilterDisplayedTagsToMostInterestingLeafTags(): Unit = {
    val aTag = Tag(name = "A tag")
    val anotherTag = Tag(name = "Another tag")
    val childOfAnotherTag = Tag(name = "Child of another tag", parent = Some(anotherTag._id))

    val newsitem = FrontendNewsitem(id = UUID.randomUUID().toString,
      tags = Some(Seq(aTag, anotherTag, childOfAnotherTag))
    )
    assertEquals(Seq(aTag, childOfAnotherTag).asJava, newsitem.getTaggingsToShow)
  }

}
