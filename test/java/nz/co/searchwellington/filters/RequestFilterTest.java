package nz.co.searchwellington.filters;

import java.util.List;

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
	
	
    
    
    
    public void testShouldIgnoreDepreciatedCategoryParameterWithInvalidNumber() throws Exception {
        RequestFilter filter = new RequestFilter(null);         
        MockHttpServletRequest request = new MockHttpServletRequest();        
        request.setPathInfo("/rss");
        request.setParameter("category", "nz.co.searchwellington.model.Category@2a882a88");        
        filter.loadAttributesOntoRequest(request);        
    }
    
    
   
  
    

    public void testShouldExtractTagsFromUrl() throws Exception {
        // TODO incomplete test.
        final String path = "/tag/bypass+cubastreet";        
        ResourceRepository resourceDAO = null;
        RequestFilter filter = new RequestFilter(resourceDAO);
        
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
    
    
    public void testShouldExtractNewsitemsPublisherUrlWordsFromPathInfo() {
        RequestFilter filter = new RequestFilter(null);               
        String pathInfo = "/wellingtoncitycouncil/newsitems";
        assertEquals("wellingtoncitycouncil", filter.getPublisherUrlWordsFromPath(pathInfo));
    }

    
    
    public void testShouldExtractFeedsPublisherUrlWordsFromPathInfo() throws Exception {
        RequestFilter filter = new RequestFilter(null);               
        String pathInfo = "/wellingtoncitycouncil/feeds";
        assertEquals("wellingtoncitycouncil", filter.getPublisherUrlWordsFromPath(pathInfo));
    }
    
}
