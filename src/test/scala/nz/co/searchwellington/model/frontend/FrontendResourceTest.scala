package nz.co.searchwellington.model.frontend

import java.util.UUID

import nz.co.searchwellington.model.Tag
import org.junit.Assert.assertEquals
import org.junit.Test

class FrontendResourceTest {

  @Test
  def resourcesWithNoManualOrAutomaticTaggingsShowAsNotTagged(): Unit = {
    val newsitemWithNoTags = FrontendNewsitem(id = UUID.randomUUID().toString)

    assertEquals("Not tagged", newsitemWithNoTags.getTaggingStatus)

    import scala.collection.JavaConverters._
    assertEquals(Seq.empty.asJava, newsitemWithNoTags.getTaggingsToShow)
  }

  @Test
  def resourcesWithOnlyAutomaticTagsShowAsAutomaticallyTagged(): Unit = {
    val aTag = Tag()

    val newsitemWithAutomaticTagsOnly = FrontendNewsitem(id = UUID.randomUUID().toString,
      handTags = Seq.empty,
      tags = Seq(aTag)
    )

    assertEquals("Automatically tagged as: ", newsitemWithAutomaticTagsOnly.getTaggingStatus)

    import scala.collection.JavaConverters._
    assertEquals(Seq(aTag).asJava, newsitemWithAutomaticTagsOnly.getTaggingsToShow)
  }

  @Test
  def resourcesWithManualAndAutomaticTagsShowAManuallyTagged(): Unit = {
    val aTag = Tag()
    val anotherTag = Tag()

    val newsitemWithManualAndAutomaticTags = FrontendNewsitem(id = UUID.randomUUID().toString,
      handTags = Seq(aTag),
      tags = Seq(anotherTag)
    )

    assertEquals("Tagged as: ", newsitemWithManualAndAutomaticTags.getTaggingStatus)
    import scala.collection.JavaConverters._
    assertEquals(Seq(aTag).asJava, newsitemWithManualAndAutomaticTags.getTaggingsToShow)
  }

  @Test
  def shouldFilterDisplayedTagsToMostInterestingLeafTags(): Unit = {
    val aTag = Tag(name = "A tag")
    val anotherTag = Tag(name = "Another tag")
    val childOfAnotherTag = Tag(name = "Child of another tag", parent = Some(anotherTag._id))

    val newsitem = FrontendNewsitem(id = UUID.randomUUID().toString,
      tags = Seq(aTag, anotherTag, childOfAnotherTag)
    )

    import scala.collection.JavaConverters._
    assertEquals(Seq(aTag, childOfAnotherTag).asJava, newsitem.getTaggingsToShow)
  }

}
