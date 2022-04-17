package nz.co.searchwellington.utils;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class UrlFiltersTest {

    @Test
    public void canDetectHttpPrefixes() {
        assertTrue(UrlFilters.hasHttpPrefix("http://blah"));
        assertFalse(UrlFilters.hasHttpPrefix("blah"));
        assertTrue(UrlFilters.hasHttpPrefix("https://blah"));
    }

    @Test
    public void canStripPHPSessionIds() throws URISyntaxException, MalformedURLException {
        assertEquals("http://www.olympicharriers.org.nz/viewresults.php?eid=335",
                UrlFilters.stripPhpSession(new URI("http://www.olympicharriers.org.nz/viewresults.php?eid=335&PHPSESSID=e68f04603e4566f796bd0d14f3e1ba26")).toURL().toExternalForm());
        assertEquals("https://www.wcn.net.nz/news/art.php?artid=6",
                UrlFilters.stripPhpSession(new URI("https://www.wcn.net.nz/news/art.php?artid=6&PHPSESSID=1a8c3aaa31bddf8dcff8db6566879e37")).toURL().toExternalForm());
    }

}
