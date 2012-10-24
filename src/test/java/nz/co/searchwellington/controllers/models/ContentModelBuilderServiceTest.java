package nz.co.searchwellington.controllers.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.List;

import nz.co.searchwellington.controllers.CommonModelObjectsService;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.views.RssView;
import nz.co.searchwellington.views.RssViewFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.common.views.json.JsonView;

public class ContentModelBuilderServiceTest {
	
	@Mock RssViewFactory rssViewFactory;
	@Mock ViewFactory viewFactory;
	@Mock JsonCallbackNameValidator jsonCallbackNameValidator;
	@Mock CommonModelObjectsService commonModelObjectsService;
	ModelBuilder[] modelBuilders;
	
	MockHttpServletRequest request;
	@Mock RssView rssView;
	@Mock JsonView jsonView;
	@Mock List<Tag> featuredTags;
	@Mock List<Tag> topLevelTags;
	
	ModelAndView validModelAndView;
	@Mock ModelBuilder invalidModelBuilder;
	@Mock ModelBuilder validModelBuilder;
	
	ContentModelBuilderService contentModelBuilderService;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		request = new MockHttpServletRequest();
		validModelAndView = new ModelAndView();
		
		modelBuilders = new ModelBuilder[2];
		modelBuilders[0] = invalidModelBuilder;
		modelBuilders[1] = validModelBuilder;
		request.setPathInfo("/something");
		
		when(invalidModelBuilder.isValid(request)).thenReturn(false);
		when(validModelBuilder.isValid(request)).thenReturn(true);
		when(validModelBuilder.populateContentModel(request)).thenReturn(validModelAndView);

		contentModelBuilderService = new ContentModelBuilderService(rssViewFactory, 
				viewFactory, 
				jsonCallbackNameValidator,
				commonModelObjectsService,
				modelBuilders);
	}
	
	@Test
	public void shouldDelegateModelBuildingToTheFirstBuildWhoSaysTheyAreValid() throws Exception {		
		assertEquals(validModelAndView, contentModelBuilderService.populateContentModel(request));		
	}
	
	@Test
	public void shouldReturnNullIfNoModelBuilderWasFoundForRequest() throws Exception {
		modelBuilders[1] = invalidModelBuilder;
		assertNull(contentModelBuilderService.populateContentModel(request));
	}
	
	@Test
	public void rssSuffixedRequestsShouldBeGivenTheRssView() throws Exception {
		when(rssViewFactory.makeView()).thenReturn(rssView);
		request.setPathInfo("/something/rss");
		assertEquals(rssView, contentModelBuilderService.populateContentModel(request).getView());
	}
	
	@Test
	public void jsonSuffixedRequestsShouldBeGivenTheRssView() throws Exception {
		when(viewFactory.getJsonView()).thenReturn(jsonView);
		request.setPathInfo("/something/json");
		assertEquals(jsonView, contentModelBuilderService.populateContentModel(request).getView());
	}
	
	@Test
	public void commonModelElementsShouldBeAddedToHtmlViews() throws Exception {		
		// TODO how to cover?
	}
	
	@Test
	public void jsonCallbackShouldBeAddedToJsonModelIfValid() throws Exception {
		when(viewFactory.getJsonView()).thenReturn(jsonView);
		request.setPathInfo("/something/json");
		request.setParameter("callback", "validname");
		when(jsonCallbackNameValidator.isValidCallbackName(request.getParameter("callback"))).thenReturn(true);
		ModelAndView mv = contentModelBuilderService.populateContentModel(request);
		assertEquals("validname", mv.getModel().get("callback"));
	}
	
	@Test
	public void shouldRejectInvalidCallbackNames() throws Exception {
		when(viewFactory.getJsonView()).thenReturn(jsonView);
		request.setPathInfo("/something/json");
		request.setParameter("callback", "Invalid name!");
		when(jsonCallbackNameValidator.isValidCallbackName(request.getParameter("callback"))).thenReturn(false);
		ModelAndView mv = contentModelBuilderService.populateContentModel(request);
		assertNull(mv.getModel().get("callback"));
	}
	
}
