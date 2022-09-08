package nz.co.searchwellington.tagging

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.model.Category
import nz.co.searchwellington.model.{Newsitem, Tag}
import nz.co.searchwellington.repositories.TagDAO
import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse, assertTrue}
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, when}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class TagHintAutoTaggerTest extends ReasonableWaits {

  private val tagDAO = mock(classOf[TagDAO])

  private val tagHintAutoTagger = new TagHintAutoTagger(tagDAO)

  @Test
  def shouldMatchResourcesWhichContainAutotaggingHints(): Unit = {
    val tag = Tag(hints = Seq("fox", "animal"))
    val anotherTag = Tag(hints = Seq("cat"))
    when(tagDAO.getAllTags).thenReturn(Future.successful(Seq(tag, anotherTag)))
    val resource = Newsitem(id = UUID.randomUUID().toString, title = "The quick brown fox jumped over the lazy dog")

    val suggestions = Await.result(tagHintAutoTagger.suggestTags(resource), TenSeconds)

    assertTrue(suggestions.contains(tag))
    assertFalse(suggestions.contains(anotherTag))
  }

  @Test
  def autotaggingHintsShouldNotMatchSubstrings(): Unit = {
    val tag = Tag(display_name = "Art", hints = Seq("murals", "arty"))
    when(tagDAO.getAllTags).thenReturn(Future.successful(Seq(tag)))
    val resource = Newsitem(id = UUID.randomUUID().toString, title = "Party Pigs!")

    val suggestions = Await.result(tagHintAutoTagger.suggestTags(resource), TenSeconds)

    assertFalse(suggestions.contains(tag))
  }

  @Test
  def shouldNotMatchResourcesWhichOnlyContainTagName(): Unit = {
    val tag = Tag(name = "fox")
    when(tagDAO.getAllTags).thenReturn(Future.successful(Seq(tag)))
    val resource = Newsitem(id = UUID.randomUUID().toString, description = Some("The quick brown fox jumped over the lazy dog"))

    val suggestions = Await.result(tagHintAutoTagger.suggestTags(resource), TenSeconds)

    assertFalse(suggestions.contains(tag))
  }

  @Test
  def canSuggestTagsBasedOnRssCategoriesWhichMatchAutotagHints(): Unit = {
    val tag = Tag(name = "events", hints = Seq("events"))
    when(tagDAO.getAllTags).thenReturn(Future.successful(Seq(tag)))
    val feedItemCategories = Seq(Category(value = "Events", domain = None))

    val suggestions = Await.result(tagHintAutoTagger.suggestFeedCategoryTags(feedItemCategories), TenSeconds)

    assertEquals("events", suggestions.head.name)
  }

  @Test
  def rssCategoriesAreCaseInsensitive(): Unit = {
    val tag = Tag(name = "events", hints = Seq("events"))
    when(tagDAO.getAllTags).thenReturn(Future.successful(Seq(tag)))
    val feedItemCategories = Seq(Category(value = "Events", domain = None))

    val suggestions = Await.result(tagHintAutoTagger.suggestFeedCategoryTags(feedItemCategories), TenSeconds)

    assertEquals("events", suggestions.head.name)
  }

}