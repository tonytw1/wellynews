package nz.co.searchwellington.tagging;

import java.util.HashSet;

import junit.framework.TestCase;

import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagImpl;
import nz.co.searchwellington.repositories.ResourceRepository;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;


public class PlaceAutoTaggerTest extends TestCase {
    
    private Tag placesTag;
    private Tag aroValleyTag;
    
    ResourceRepository resourceDAO;
    AutoTaggingService autoTaggingService;
    Newsitem aroValleyNewsitem;
    
    @Override
    protected void setUp() throws Exception {
        placesTag =  new TagImpl(1, "places","Places", null, new HashSet<Tag>(), 0);       
        aroValleyTag = new TagImpl(2, "arovalley","Aro Valley", placesTag, new HashSet<Tag>(), 0);
        placesTag.addChild(aroValleyTag);
        
        resourceDAO = createMock(ResourceRepository.class);
        expect(resourceDAO.loadTagByName("places")).andReturn(placesTag);
        replay(resourceDAO);               
    }
    
    
    public void testShouldTagNewsitemsWithPlaceTags() throws Exception {     
        aroValleyNewsitem = new NewsitemImpl(1, "Test newsitem", null, ".. Student flats in the Aro Valley... Test", null, null, new HashSet<Tag>(), null);       
        
        AutoTaggingService autoTaggingService = createMock(AutoTaggingService.class);
        expect(autoTaggingService.alreadyHasTag(aroValleyNewsitem, aroValleyTag)).andReturn(false);
        replay(autoTaggingService);
              
        PlaceAutoTagger placeAutoTagger = new PlaceAutoTagger(resourceDAO, autoTaggingService);        
        placeAutoTagger.tag(aroValleyNewsitem);        
        assertTrue(aroValleyNewsitem.getTags().contains(aroValleyTag));        
    }

    
   
    public void testPlaceAutoTaggingShouldBeCaseInsensitive() throws Exception {                
        Newsitem aroValleyNewsitem = new NewsitemImpl(1, "Test newsitem", null, ".. Student flats in the aro valley... Test", null, null, new HashSet<Tag>(), null);
        
        AutoTaggingService autoTaggingService = createMock(AutoTaggingService.class);
        expect(autoTaggingService.alreadyHasTag(aroValleyNewsitem, aroValleyTag)).andReturn(false);
        replay(autoTaggingService);
       
        PlaceAutoTagger placeAutoTagger = new PlaceAutoTagger(resourceDAO, autoTaggingService); 
        placeAutoTagger.tag(aroValleyNewsitem);               
        assertTrue(aroValleyNewsitem.getTags().contains(aroValleyTag));
    }
   

}
