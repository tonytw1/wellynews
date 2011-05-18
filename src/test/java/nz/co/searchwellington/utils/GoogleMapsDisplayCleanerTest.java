package nz.co.searchwellington.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.views.GoogleMapsDisplayCleaner;

public class GoogleMapsDisplayCleanerTest extends TestCase {

    Geocode here = new Geocode("here", 1, 1);
    Geocode there = new Geocode("there", 2, 2);
    Geocode alsoHere = new Geocode("here", 1, 1);
    
    List<Resource> geocoded; 
    Resource firstNewsitem;
    Resource secondNewsitem;
    Resource thirdNewsitem;
        
    GoogleMapsDisplayCleaner cleaner = new GoogleMapsDisplayCleaner();    
    
    public void setUp() throws Exception {
        geocoded = new ArrayList<Resource>();
        firstNewsitem = new NewsitemImpl();
        firstNewsitem.setGeocode(here);
        secondNewsitem = new NewsitemImpl();
        secondNewsitem.setGeocode(there);
        thirdNewsitem = new NewsitemImpl();
        thirdNewsitem.setGeocode(alsoHere);        
        geocoded.addAll(Arrays.asList(firstNewsitem, secondNewsitem, thirdNewsitem));
    }
    
    
    public void testShouldDedupeListByGeocodeSoThatLowerItemsDoNotOverlayEarlierOnes() throws Exception {                              
        List<Resource> deduped = cleaner.dedupe(geocoded);
        assertFalse(deduped.contains(thirdNewsitem));       
        assertTrue(deduped.size() == 2);
    }
    
    
    public void testShouldPutSelectedItemInFirst() throws Exception {        
        Resource selected = new NewsitemImpl();
        selected.setGeocode(there);
        
        List<Resource> deduped = cleaner.dedupe(geocoded, selected);
        assertTrue(deduped.contains(selected));
        assertFalse(deduped.contains(secondNewsitem));        
    }
    
}
