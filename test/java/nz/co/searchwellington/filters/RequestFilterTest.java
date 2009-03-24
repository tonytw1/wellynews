package nz.co.searchwellington.filters;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.springframework.mock.web.MockHttpServletRequest;

public class RequestFilterTest extends TestCase {

	/*public void testShouldResolveResourceFromGeotaggedPageUrl() throws Exception {		
		Resource geotagged = new NewsitemImpl();
		geotagged.setId(123);

		MockHttpServletRequest request = new MockHttpServletRequest(); 
		
		// TODO want this; not ?resource=123
	    request.setPathInfo("/geotagged/123");
		
		ResourceRepository resourceDAO = null;		
		RequestFilter filter = new RequestFilter(resourceDAO);       
			
		filter.loadAttributesOntoRequest(request);
		
		assertNotNull(request.getAttribute("resource"));		
	}
	*/
	
	
    public void testTagsShouldComeBackInOrder() throws Exception {
        RequestFilter filter = new RequestFilter(null);        
        List <String> tags = filter.getTagNamesFromPath("/tag/a+b");
        assertEquals(2, tags.size());
        assertEquals("a", tags.get(0));
        assertEquals("b", tags.get(1));
    }
    
    
    public void testShouldIgnoreDepreciatedCategoryParameterWithInvalidNumber() throws Exception {
        RequestFilter filter = new RequestFilter(null);         
        MockHttpServletRequest request = new MockHttpServletRequest();        
        request.setPathInfo("/rss");
        request.setParameter("category", "nz.co.searchwellington.model.Category@2a882a88");        
        filter.loadAttributesOntoRequest(request);        
    }
    
    
    public void testShouldGetTagnameFromTagControllerPath() throws Exception {    
        RequestFilter filter = new RequestFilter(null);        
        List <String> tags = filter.getTagNamesFromPath("/tag/testtag");
        assertEquals(1, tags.size());
        assertTrue(tags.contains("testtag")); 
    }
    
    
    public void testShouldGetBothTagNamesFromTagControllerPath() throws Exception {    
        RequestFilter filter = new RequestFilter(null);        
        List <String> tags = filter.getTagNamesFromPath("/tag/testtag+anothertag");
        assertEquals(2, tags.size());
        assertTrue(tags.contains("testtag"));
        assertTrue(tags.contains("anothertag")); 
    }
    

    public void testShouldExtractTagsFromUrl() throws Exception {
        // TODO incomplete test.
        final String path = "/tag/bypass+cubastreet";        
        ResourceRepository resourceDAO = null;
        RequestFilter filter = new RequestFilter(resourceDAO);
        
    }
    
    public void testShouldGetTagnameFromRssControllerPath() throws Exception {    
        RequestFilter filter = new RequestFilter(null);
        assertTrue(filter.getTagNamesFromPath("/rss/tag/testtag").contains("testtag")); 
    }
    
    
    public void testShouldGetTagnameFromTagNewsArchiveUrl() throws Exception {    
        RequestFilter filter = new RequestFilter(null);
        assertTrue(filter.getTagNamesFromPath("/tag/testtag/news").contains("testtag")); 
    }
    
    
    public void testShouldBeAbleToGetResourceFromPublicTaggingUrl() throws Exception {        
        RequestFilter filter = new RequestFilter(null);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("resource", "123");
        assertEquals(123, filter.parseResourceIDFromRequestParameter(request).intValue());        
    }
        
        
    public void testShouldParseArchiveDateFromArchiveUrl() throws Exception {
        RequestFilter filter = new RequestFilter(null);
        assertEquals("1 Mar 2006 00:00:00 GMT", filter.getArchiveDateFromPath("/archive/2006/mar").toGMTString());
        assertEquals("1 Mar 2006 00:00:00 GMT", filter.getArchiveDateFromPath("/archive/2006/mar/").toGMTString());        
    }
    
    
    public void testShouldExtractRssTypeFromRssUrl() throws Exception {
        RequestFilter filter = new RequestFilter(null);         
        assertEquals("L", filter.getRssTypeFromRequest("/rss/watchlist"));
        assertEquals("W", filter.getRssTypeFromRequest("/rss/justin"));
    }
    
    
    
    public void testShouldFindTagOnEditTagUrl() throws Exception {        
        RequestFilter filter = new RequestFilter(null);        
        List <String> tags = filter.getTagNamesFromPath("/edit/tag/waterwhirler");
        assertEquals(1, tags.size());
        assertTrue(tags.contains("waterwhirler")); 
    }
    
    
    public void testShouldExtractNewsitemsPublisherUrlWordsFromPathInfo() {
        RequestFilter filter = new RequestFilter(null);               
        String pathInfo = "/wellingtoncitycouncil/newsitems";
        assertEquals("wellingtoncitycouncil", filter.getPublisherUrlWordsFromPath(pathInfo));
    }
    
    
    
}
