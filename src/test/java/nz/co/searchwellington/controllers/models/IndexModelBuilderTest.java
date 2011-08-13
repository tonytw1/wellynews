package nz.co.searchwellington.controllers.models;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.controllers.models.helpers.ArchiveLinksService;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.ModelAndView;

public class IndexModelBuilderTest {

	@Mock ContentRetrievalService contentRetrievalService;
	@Mock RssUrlBuilder rssUrlBuilder;
	@Mock LoggedInUserFilter loggedInUserFilter;
	@Mock UrlBuilder urlBuilder;
	@Mock ArchiveLinksService archiveLinksService;
	@Mock HttpServletRequest request;

	@Mock List<Tag> featuredTags;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void featuredTagsShouldBeShownAsExtraContent() throws Exception {
		Mockito.when(contentRetrievalService.getFeaturedTags()).thenReturn(featuredTags);
		
		IndexModelBuilder modelBuilder = new IndexModelBuilder(contentRetrievalService, rssUrlBuilder, loggedInUserFilter, urlBuilder, archiveLinksService);
		
		ModelAndView mv = new ModelAndView();
		modelBuilder.populateExtraModelConent(request, false, mv);
				
		assertEquals(featuredTags, mv.getModel().get("featuredTags"));
	}
	
}
