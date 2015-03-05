package nz.co.searchwellington.tagging;

import com.google.common.collect.Lists;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.TagDAO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class TagHintAutoTaggerTest {

    @Mock
    private TagDAO tagDAO;

    private TagHintAutoTagger tagHintAutoTagger;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        tagHintAutoTagger = new TagHintAutoTagger(tagDAO);
    }

    @Test
    public void shouldMatchTitlesWhichContainAutotaggingHint() {
        Tag tag = new Tag();
        tag.setAutotagHints("fox,animal");
        Tag anotherTag = new Tag();
        anotherTag.setAutotagHints("cat");

        List<Tag> allTags = Lists.newArrayList(tag, anotherTag);
        when(tagDAO.getAllTags()).thenReturn(allTags);

        Resource resource = new NewsitemImpl();
        resource.setName("The quick brown fox jumped over the lazy dog");

        Set<Tag> suggestions = tagHintAutoTagger.suggestTags(resource);

        assertTrue(suggestions.contains(tag));
        assertFalse(suggestions.contains(anotherTag));
    }

}