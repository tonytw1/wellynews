package nz.co.searchwellington.tagging

import com.google.common.collect.Lists
import nz.co.searchwellington.model.{TagBuilder, NewsitemImpl, Resource, Tag}
import nz.co.searchwellington.repositories.TagDAO
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.List
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
    val tag: Tag = new TagBuilder().autotagHints("fox,animal").build
    val anotherTag: Tag = new TagBuilder().autotagHints("cat").build
    val allTags: List[Tag] = Lists.newArrayList(tag, anotherTag)
    when(tagDAO.getAllTags).thenReturn(allTags)

    val resource: Resource = new NewsitemImpl
    resource.setName("The quick brown fox jumped over the lazy dog")

    val suggestions: Set[Tag] = tagHintAutoTagger.suggestTags(resource)

    assertTrue(suggestions.contains(tag))
    assertFalse(suggestions.contains(anotherTag))
  }

}