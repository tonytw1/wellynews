package nz.co.searchwellington.controllers.submission;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.utils.UrlCleaner;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;

public class UrlProcessorTest {
	
	@Mock UrlCleaner urlCleaner;
	@Mock Resource resource;
	MockHttpServletRequest request;
	
	UrlProcessor processor;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		processor = new UrlProcessor(urlCleaner);
		request = new MockHttpServletRequest();
	}
	
	@Test
	public void urlShouldBeCleanedBeAttachingToResource() throws Exception {
		when(urlCleaner.cleanSubmittedItemUrl("www.blah ")).thenReturn("http://www.blah");		
		request.setParameter("url", "www.blah ");		
		processor.process(request, resource);		
		verify(resource).setUrl("http://www.blah");
	}

}
