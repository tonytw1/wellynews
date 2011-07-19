package nz.co.searchwellington.controllers;

import nz.co.searchwellington.geocoding.GeoCodeService;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.HandTaggingDAO;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.TagDAO;
import nz.co.searchwellington.utils.UrlCleaner;

import org.apache.struts.mock.MockHttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class SubmissionProcessingServiceTest {

	@Mock UrlCleaner urlCleaner;
	@Mock GeoCodeService geocodeService;
	@Mock TagDAO tagDAO;
	@Mock HandTaggingDAO tagVoteDAO;
	@Mock ResourceRepository resourceDAO;
	@Mock Resource resource;
	
	private MockHttpServletRequest request;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		request = new MockHttpServletRequest();
	}
	
	@Test
	public void shouldFlattenLoudCapsHeadlinesInUserSubmissions() throws Exception {
		SubmissionProcessingService submissionProcessingService = new SubmissionProcessingService(urlCleaner, geocodeService, tagDAO, tagVoteDAO, resourceDAO);
		
		request.addParameter("title", "THE QUICK BROWN FOX JUMPED OVER THE LAZY DOG");
		submissionProcessingService.processTitle(request, resource);
		
		Mockito.verify(resource).setName("The quick brown fox jumped over the lazy dog");
	}
	
}
