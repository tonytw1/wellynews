package nz.co.searchwellington.controllers;

import static org.junit.Assert.assertEquals;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.Tag;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class RssUrlBuilderTest {

	@Mock SiteInformation siteInformation;
	@Mock Tag tag;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(siteInformation.getAreaname()).thenReturn("Somewhere");
	}
	
	@Test
	public void rssDescriptionForTagShouldBeSetFromTagDescriptionTextIfAvailable() throws Exception {
		RssUrlBuilder rssUrlBuilder = new RssUrlBuilder(siteInformation);
		
		Mockito.when(tag.getDescription()).thenReturn("This is a tag about something...");
		
		assertEquals(tag.getDescription(), rssUrlBuilder.getRssDescriptionForTag(tag));
	}
	
}
