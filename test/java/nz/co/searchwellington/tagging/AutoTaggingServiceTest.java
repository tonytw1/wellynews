package nz.co.searchwellington.tagging;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.WebsiteImpl;

public class AutoTaggingServiceTest extends TestCase {

    
    public void testShouldNotApplyExtraTagsToNewsitemWhichAlreadyHasPublisherTag() throws Exception {
        Tag publishersTag = new Tag();
        
        Website publisher = new WebsiteImpl();
        Set<Tag> publishersTags = new HashSet<Tag>();
        publishersTags.add(publishersTag);
        publisher.setTags(publishersTags);
        
        Newsitem newsitem = new NewsitemImpl();
        newsitem.setTags(new HashSet<Tag>());
        newsitem.setPublisher(publisher);
                
        AutoTaggingService service = new AutoTaggingService();
        service.applyTag(newsitem, publishersTag);    
        assertFalse(newsitem.getTags().contains(publishersTag)); 
        
        assertTrue(service.alreadyHasTag(newsitem, publishersTag));
    }
}
