package nz.co.searchwellington.caching;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.FeedNewsitem;

import org.junit.Before;
import org.junit.Test;

public class MemcachedCacheFunctionalTest {

	private static final String UPDATED_VALUE = "Updated value";
	private static final int TTL = 60;
	private static final String KEY = "testkey3";
	private static final String FEED_KEY = "feedkey2";
	private static final Object TEST_STRING = "Test String";
	private static final String HEADLINE = "Test headline";

	private MemcachedCache cache;
	
	//@Before
	public void setup() {
		cache = new MemcachedCache();
		cache.setMemcachedUrls("localhost:11211");
	}
	
	//@Test
	public void canRoundTripSimpleContentThroughTheCache() throws Exception {		
		cache.put(KEY, 3600, TEST_STRING);		
		assertEquals(TEST_STRING, cache.get(KEY));		
	}
	
	//@Test
	public void subsequentPutsShouldOverwriteExistingValue() throws Exception {
		cache.put(KEY, 3600, TEST_STRING);
		assertEquals(TEST_STRING, cache.get(KEY));		
		cache.put(KEY, 3600, UPDATED_VALUE);
		assertEquals(UPDATED_VALUE, cache.get(KEY));
	}
	
	@SuppressWarnings("unchecked")
	//@Test
	public void canRoundTripMoreInterestingObjectsThroughTheCache() throws Exception {
		List<FeedNewsitem> feednewsItems = new ArrayList<FeedNewsitem>();
		FeedNewsitem feedNewsitem = new FeedNewsitem();
		feedNewsitem.setId(1);
		feedNewsitem.setName(HEADLINE);
		feednewsItems.add(feedNewsitem);
		assertEquals(HEADLINE, feednewsItems.get(0).getName());

		cache.put(FEED_KEY, TTL, feednewsItems);
		
		List<FeedNewsitem> retrieved = (List<FeedNewsitem>) cache.get(FEED_KEY);
		assertEquals(1, retrieved.size());
		assertEquals(HEADLINE, retrieved.get(0).getName());
	}
	
}
