package nz.co.searchwellington.tagging

import nz.co.searchwellington.model.{Newsitem, TagBuilder}
import nz.co.searchwellington.repositories.TagDAO
import org.junit.Assert.{assertFalse, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.when
import org.mockito.{Mock, MockitoAnnotations}

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
    when(tagDAO.getAllTags).thenReturn(Seq(tag, anotherTag))
    val resource = Newsitem(title = Some("The quick brown fox jumped over the lazy dog"))

    val suggestions = tagHintAutoTagger.suggestTags(resource)

    assertTrue(suggestions.contains(tag))
    assertFalse(suggestions.contains(anotherTag))
  }

}