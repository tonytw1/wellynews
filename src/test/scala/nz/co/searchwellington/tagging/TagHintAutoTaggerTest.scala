package nz.co.searchwellington.tagging

import java.util.UUID

import nz.co.searchwellington.model.{Newsitem, TagBuilder}
import nz.co.searchwellington.repositories.TagDAO
import org.junit.Assert.{assertFalse, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.when
import org.mockito.{Mock, MockitoAnnotations}

import scala.concurrent.Future

class TagHintAutoTaggerTest {

  @Mock private var tagDAO: TagDAO = null

  private var tagHintAutoTagger: TagHintAutoTagger = null

  @Before def setup {
    MockitoAnnotations.initMocks(this)
    tagHintAutoTagger = new TagHintAutoTagger(tagDAO)
  }

  @Test def shouldMatchTitlesWhichContainAutotaggingHint {
    val tag = new TagBuilder().autotagHints("fox,animal").build
    val anotherTag = new TagBuilder().autotagHints("cat").build
    when(tagDAO.getAllTags).thenReturn(Future.successful(Seq(tag, anotherTag)))
    val resource = Newsitem(id = UUID.randomUUID().toString, title = Some("The quick brown fox jumped over the lazy dog"))

    val suggestions = tagHintAutoTagger.suggestTags(resource)

    assertTrue(suggestions.contains(tag))
    assertFalse(suggestions.contains(anotherTag))
  }

}