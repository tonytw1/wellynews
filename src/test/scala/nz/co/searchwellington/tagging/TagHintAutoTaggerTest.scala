package nz.co.searchwellington.tagging

import com.google.common.collect.Lists
import nz.co.searchwellington.model.{TagBuilder, NewsitemImpl, Resource, Tag}
import nz.co.searchwellington.repositories.TagDAO
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.mockito.Mockito.when

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
    when(tagDAO.getAllTags).thenReturn(Lists.newArrayList(tag, anotherTag))

    val resource: Resource = new NewsitemImpl
    resource.setName("The quick brown fox jumped over the lazy dog")

    val suggestions = tagHintAutoTagger.suggestTags(resource)

    assertTrue(suggestions.contains(tag))
    assertFalse(suggestions.contains(anotherTag))
  }

}