package nz.co.searchwellington.tagging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;

public class TagInformationServiceTest extends TestCase {
    
    Tag tag1 = new Tag();
    Tag tag2 = new Tag();
    Tag tag3 = new Tag();
    Tag tag4 = new Tag();
    
    Newsitem newsitem1 = new NewsitemImpl();
    Newsitem newsitem2 = new NewsitemImpl();
    Newsitem newsitem3 = new NewsitemImpl();
    Newsitem newsitem4 = new NewsitemImpl();
    Newsitem newsitem5 = new NewsitemImpl();
    
    List<Newsitem> newsitems = new ArrayList<Newsitem>();

    TagInformationService service = new TagInformationService();
    
    @Override
    protected void setUp() throws Exception {
        
        tag1.setName("tag1");
        tag2.setName("tag2");
        tag3.setName("tag3");
        tag4.setName("tag4");
        
        newsitem1.setTags(new HashSet<Tag>(Arrays.asList(tag1, tag2, tag3)));
        newsitem2.setTags(new HashSet<Tag>(Arrays.asList(tag3)));
        newsitem3.setTags(new HashSet<Tag>(Arrays.asList(tag2, tag1)));
        newsitem4.setTags(new HashSet<Tag>(Arrays.asList(tag3)));
        newsitem5.setTags(new HashSet<Tag>(Arrays.asList(tag4)));
        
        newsitems.add(newsitem1);
        newsitems.add(newsitem2);
        newsitems.add(newsitem3);
        newsitems.add(newsitem4);
        newsitems.add(newsitem5);       
    }
    
    public void testShouldLimitNumberOfResults() throws Exception {          
        assertEquals(3, service.getNewsitemsMostUsedTags(newsitems, 3).size());
    }

    
    public void testShouldOrderCorrectly() throws Exception {                
        List<TagContentCount> usedTags = service.getNewsitemsMostUsedTags(newsitems, 3);
                
        assertEquals(tag3, usedTags.get(0).getTag());
        assertEquals(3, usedTags.get(0).getCount());
    }
    
    public void testShouldCalculatePercentagesCorrectly() throws Exception {		
    	assertEquals(50, service.calculatePercentage(100, 50));    	    	
	}
    
    
    
    
}
