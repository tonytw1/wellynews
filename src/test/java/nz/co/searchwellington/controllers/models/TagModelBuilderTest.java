package nz.co.searchwellington.controllers.models;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import nz.co.searchwellington.controllers.RelatedTagsService;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.flickr.FlickrService;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.repositories.solr.KeywordSearchService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

public class TagModelBuilderTest {

	@Mock ContentRetrievalService contentRetrievalService;
	@Mock RssUrlBuilder rssUrlBuilder;
	@Mock UrlBuilder urlBuilder;
	@Mock RelatedTagsService relatedTagsService;
	@Mock ConfigRepository configDAO;
	@Mock RssfeedNewsitemService rssfeedNewsitemService;
	@Mock KeywordSearchService keywordSearchService;
	@Mock FlickrService flickrService;

	private MockHttpServletRequest  request;
	@Mock List<FrontendResource> tagNewsitems;	
	@Mock Tag tag;

	private TagModelBuilder modelBuilder;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		modelBuilder = new TagModelBuilder(rssUrlBuilder, urlBuilder, relatedTagsService, configDAO, rssfeedNewsitemService, contentRetrievalService, keywordSearchService, flickrService);
		request = new MockHttpServletRequest();
	}
	
	@Test
	public void mainContentShouldBeTagNewsitems() throws Exception {
		request.setAttribute("tags", Arrays.asList(tag));
		Mockito.when(contentRetrievalService.getTaggedNewsitems(tag, 0, 30)).thenReturn(tagNewsitems);
		ModelAndView mv = modelBuilder.populateContentModel(request, false);
		
		assertEquals(tagNewsitems, mv.getModel().get("main_content"));
	}
	
}
