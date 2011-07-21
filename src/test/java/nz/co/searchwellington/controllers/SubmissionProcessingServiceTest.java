package nz.co.searchwellington.controllers;

import nz.co.searchwellington.geocoding.GeoCodeService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.UrlWordsGenerator;
import nz.co.searchwellington.model.User;
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

	private static final String FEED_NAME = "A feed";
	@Mock UrlCleaner urlCleaner;
	@Mock GeoCodeService geocodeService;
	@Mock TagDAO tagDAO;
	@Mock HandTaggingDAO tagVoteDAO;
	@Mock ResourceRepository resourceDAO;
	@Mock Newsitem resource;
	
	private MockHttpServletRequest request;
	@Mock Feed feed;
	@Mock User loggedInUser;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		request = new MockHttpServletRequest();
		Mockito.when(feed.getName()).thenReturn(FEED_NAME);
	}
	
	@Test
	public void shouldFlattenLoudCapsHeadlinesInUserSubmissions() throws Exception {
		SubmissionProcessingService submissionProcessingService = new SubmissionProcessingService(urlCleaner, geocodeService, tagDAO, tagVoteDAO, resourceDAO);
		
		request.addParameter("title", "THE QUICK BROWN FOX JUMPED OVER THE LAZY DOG");
		submissionProcessingService.processTitle(request, resource);
		
		Mockito.verify(resource).setName("The quick brown fox jumped over the lazy dog");
	}
	
	@Test
	public void shouldPopulateAcceptanceFieldsOnInitalSubmission() throws Exception {
		SubmissionProcessingService submissionProcessingService = new SubmissionProcessingService(urlCleaner, geocodeService, tagDAO, tagVoteDAO, resourceDAO);
		request.addParameter("acceptedFromFeed", UrlWordsGenerator.makeUrlWordsFromName(FEED_NAME));
		
		Mockito.when(resourceDAO.loadFeedByUrlWords("a-feed")).thenReturn(feed);
		submissionProcessingService.processAcceptance(request, resource, loggedInUser);
		
		Mockito.verify(resource).setFeed(feed);
		Mockito.verify(resource).setAcceptedBy(loggedInUser);
		// TODO assert acceptance time was set!
	}
	
}
