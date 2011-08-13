package nz.co.searchwellington.tagging;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.TagDAO;

public class PlaceAutoTaggerTest extends TestCase {
    
    private Tag placesTag;
    private Tag aroValleyTag;
    
    private PlaceAutoTagger placeAutoTagger;
    private Newsitem aroValleyNewsitem;    
    private TagDAO tagDAO = mock(TagDAO.class);
        
    @Override
    protected void setUp() throws Exception {
        placesTag =  new Tag(1, "places","Places", null, new HashSet<Tag>(), 0, false, false);       
        aroValleyTag = new Tag(2, "arovalley","Aro Valley", placesTag, new HashSet<Tag>(), 0, false, false);
        placesTag.addChild(aroValleyTag);
        placeAutoTagger = new PlaceAutoTagger(tagDAO);
        when(tagDAO.loadTagByName("places")).thenReturn(placesTag);        
    }
    
    public void testShouldTagNewsitemsWithPlaceTags() throws Exception {
        aroValleyNewsitem = new NewsitemImpl(1, "Test newsitem", null, ".. Student flats in the Aro Valley... Test", null, null, null, null, null);           
        Set<Tag> suggestedTags = placeAutoTagger.suggestTags(aroValleyNewsitem);
		assertTrue(suggestedTags.contains(aroValleyTag));
    }
    
    public void testPlaceAutoTaggingShouldBeCaseInsensitive() throws Exception {
        aroValleyNewsitem = new NewsitemImpl(1, "Test newsitem", null, ".. Student flats in the aro valley... Test", null, null, null, null, null);           
        Set<Tag> suggestedTags = placeAutoTagger.suggestTags(aroValleyNewsitem);
		assertTrue(suggestedTags.contains(aroValleyTag));
    }
   
}
