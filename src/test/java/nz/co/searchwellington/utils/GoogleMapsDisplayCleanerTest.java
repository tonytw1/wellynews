package nz.co.searchwellington.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nz.co.searchwellington.geo.DistanceMeasuringService;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.views.GoogleMapsDisplayCleaner;

import org.junit.Before;
import org.junit.Test;

public class GoogleMapsDisplayCleanerTest {
	
	private Geocode here = new Geocode("here", 1.1, 1.1);
	private Geocode there = new Geocode("there", 2.2, 2.2);
	private Geocode alsoHere = new Geocode("here", 1.1, 1.1);
    
	private List<FrontendResource> geocoded; 
	private Resource firstNewsitem;
	private Resource secondNewsitem;
	private Resource thirdNewsitem;
            
    private GoogleMapsDisplayCleaner cleaner;
    
    @Before
    public void setup() throws Exception {
        geocoded = new ArrayList<FrontendResource>();
        firstNewsitem = new NewsitemImpl();
        firstNewsitem.setName("First");
        firstNewsitem.setGeocode(here);
        secondNewsitem = new NewsitemImpl();
        secondNewsitem.setName("Second");
        secondNewsitem.setGeocode(there);
        thirdNewsitem = new NewsitemImpl();
        thirdNewsitem.setName("Third");
        thirdNewsitem.setGeocode(alsoHere);        
        geocoded.addAll(Arrays.asList(firstNewsitem, secondNewsitem, thirdNewsitem));
        
       cleaner = new GoogleMapsDisplayCleaner(new DistanceMeasuringService());
    }
    
    @Test
    public void shouldDedupeListByGeocodeSoThatLowerItemsDoNotOverlayEarlierOnes() throws Exception {                              
        List<FrontendResource> deduped = cleaner.dedupe(geocoded);
        assertFalse(deduped.contains(thirdNewsitem));       
        assertTrue(deduped.size() == 2);
    }
    
    @Test
    public void shouldPutSelectedItemInFirst() throws Exception {        
        Resource selected = new NewsitemImpl();
        selected.setGeocode(there);
        
        List<FrontendResource> deduped = cleaner.dedupe(geocoded, selected);
        assertTrue(deduped.contains(selected));
        assertFalse(deduped.contains(secondNewsitem));        
    }
    
}
