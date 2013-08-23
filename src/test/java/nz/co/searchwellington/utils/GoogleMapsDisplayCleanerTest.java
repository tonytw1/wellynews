package nz.co.searchwellington.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import nz.co.searchwellington.model.frontend.FrontendNewsitemImpl;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.views.GoogleMapsDisplayCleaner;

import org.junit.Before;
import org.junit.Test;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.geo.model.Place;

import com.google.common.collect.Lists;

public class GoogleMapsDisplayCleanerTest {
	
	private Place here = new Place("here", new LatLong(1.1, 1.1), null);
	private Place there = new Place("there", new LatLong(2.2, 2.2), null);
	private Place alsoHere = new Place("here", new LatLong(1.1, 1.1), null);
    
	private List<FrontendResource> geocoded; 
	private FrontendNewsitemImpl firstNewsitem;
	private FrontendNewsitemImpl secondNewsitem;
	private FrontendNewsitemImpl thirdNewsitem;
            
    private GoogleMapsDisplayCleaner cleaner;
    
    @Before
    public void setup() throws Exception {
        geocoded = Lists.newArrayList();
        firstNewsitem = new FrontendNewsitemImpl();
        firstNewsitem.setName("First");
        firstNewsitem.setPlace(here);
        secondNewsitem = new FrontendNewsitemImpl();
        secondNewsitem.setName("Second");
        secondNewsitem.setPlace(there);
        thirdNewsitem = new FrontendNewsitemImpl();
        thirdNewsitem.setName("Third");
        thirdNewsitem.setPlace(alsoHere);        
        geocoded.addAll(Arrays.asList(firstNewsitem, secondNewsitem, thirdNewsitem));
        
        cleaner = new GoogleMapsDisplayCleaner();
    }
    
    @Test
    public void shouldDedupeListByGeocodeSoThatLowerItemsDoNotOverlayEarlierOnes() throws Exception {                              
        List<FrontendResource> deduped = cleaner.dedupe(geocoded);
        assertFalse(deduped.contains(thirdNewsitem));       
        assertTrue(deduped.size() == 2);
    }
    
    @Test
    public void shouldPutSelectedItemInFirst() throws Exception {        
        FrontendNewsitemImpl selected = new FrontendNewsitemImpl();
        selected.setPlace(there);
        
        List<FrontendResource> deduped = cleaner.dedupe(geocoded, selected);
        assertTrue(deduped.contains(selected));
        assertFalse(deduped.contains(secondNewsitem));        
    }
    
}
