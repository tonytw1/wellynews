package nz.co.searchwellington.tagging

import com.google.common.collect.Lists
import nz.co.searchwellington.model.NewsitemImpl
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.model.Tag
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
    val tag = new Tag().autotagHints("fox,animal")
    val anotherTag = new Tag().autotagHints("cat")
    when(tagDAO.getAllTags).thenReturn(Lists.newArrayList(tag, anotherTag))

    val resource = new NewsitemImpl().name("The quick brown fox jumped over the lazy dog")

    val suggestions = tagHintAutoTagger.suggestTags(resource)

    assertTrue(suggestions.contains(tag))
    assertFalse(suggestions.contains(anotherTag))
  }

}