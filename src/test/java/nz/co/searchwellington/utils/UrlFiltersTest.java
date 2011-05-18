package nz.co.searchwellington.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class UrlFiltersTest {
	
	@Test
	public void canDetectHttpPrefixes() throws Exception {
		assertTrue(UrlFilters.hasHttpPrefix("http://blah"));
		assertFalse(UrlFilters.hasHttpPrefix("blah"));		
		assertTrue(UrlFilters.hasHttpPrefix("https://blah"));
	}

}
