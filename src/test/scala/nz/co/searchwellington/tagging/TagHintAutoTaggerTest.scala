package nz.co.searchwellington.tagging

import java.util.UUID

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Newsitem, Tag}
import nz.co.searchwellington.repositories.TagDAO
import org.junit.Assert.{assertFalse, assertTrue}
import org.junit.Test
import org.mockito.Mockito.{mock, when}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class TagHintAutoTaggerTest extends ReasonableWaits {

  private val tagDAO = mock(classOf[TagDAO])

  private val tagHintAutoTagger = new TagHintAutoTagger(tagDAO)

  @Test
  def shouldMatchResourcesWhichContainAutotaggingHints() {
    val tag = Tag(autotag_hints = Some("fox,animal"))
    val anotherTag = Tag(autotag_hints = Some("cat"))
    when(tagDAO.getAllTags).thenReturn(Future.successful(Seq(tag, anotherTag)))
    val resource = Newsitem(id = UUID.randomUUID().toString, title = Some("The quick brown fox jumped over the lazy dog"))

    val suggestions = Await.result(tagHintAutoTagger.suggestTags(resource), TenSeconds)

    assertTrue(suggestions.contains(tag))
    assertFalse(suggestions.contains(anotherTag))
  }

  @Test
  def shouldNotMatchResourcesWhichOnlyContainTagName() {
    val tag = Tag(name = "fox")
    when(tagDAO.getAllTags).thenReturn(Future.successful(Seq(tag)))
    val resource = Newsitem(id = UUID.randomUUID().toString, description = Some("The quick brown fox jumped over the lazy dog"))

    val suggestions = Await.result(tagHintAutoTagger.suggestTags(resource), TenSeconds)

    assertFalse(suggestions.contains(tag))
  }

}