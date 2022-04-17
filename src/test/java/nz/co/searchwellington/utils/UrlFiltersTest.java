package nz.co.searchwellington.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class UrlFiltersTest {

    @Test
    public void canDetectHttpPrefixes() {
        assertTrue(UrlFilters.hasHttpPrefix("http://blah"));
        assertFalse(UrlFilters.hasHttpPrefix("blah"));
        assertTrue(UrlFilters.hasHttpPrefix("https://blah"));
    }

	@Test
	public void canStripPHPSessionIds() {
		assertEquals("http://www.olympicharriers.org.nz/viewresults.php?eid=335",
				UrlFilters.stripPhpSession("http://www.olympicharriers.org.nz/viewresults.php?eid=335&PHPSESSID=e68f04603e4566f796bd0d14f3e1ba26"));
		assertEquals("https://www.wcn.net.nz/news/art.php?artid=6",
				UrlFilters.stripPhpSession("https://www.wcn.net.nz/news/art.php?artid=6&PHPSESSID=1a8c3aaa31bddf8dcff8db6566879e37"));
	}

}
