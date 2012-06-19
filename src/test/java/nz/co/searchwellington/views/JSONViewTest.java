package nz.co.searchwellington.views;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;
import nz.co.searchwellington.model.frontend.FrontendNewsitemImpl;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JsonViewTest {
	
	private JsonSerializer jsonSerializer = new JsonSerializer();
	private EtagGenerator etagGenerator = new EtagGenerator();
	
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private Map<String, Object> mv;
	
	private JsonView view;
	
	@Before
	public void setUp() throws Exception {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		mv = new HashMap<String, Object>();
		view = new JsonView(jsonSerializer, etagGenerator);
	}
	
	@Test
	public void shouldIncludeDescriptionFieldIfAvailable() throws Exception {
		List<FrontendNewsitem> mainContent = new ArrayList<FrontendNewsitem>();
		mv.put("main_content", mainContent);
		mv.put("description", "Tag description");
		
		view.render(mv, request, response);
		
		final String content = response.getContentAsString();
		assertTrue(content.contains("\"description\":\"Tag description\""));	// TODO Move to a proper JSON parsing assert.
	}
	
	public void contentItemsShouldIncludePublisherField() throws Exception {
		// TODO Implement
	}
	
	@Test
	public void testShouldCorrectlyFormatDateField() throws Exception {		
		FrontendNewsitemImpl newsitem = new FrontendNewsitemImpl();
		newsitem.setName("Title");
		newsitem.setUrl("http://url");
		newsitem.setDate(new DateTime(2009, 4, 24, 0, 0, 0, 0).toDate());
		
		List<FrontendNewsitem> mainContent = new ArrayList<FrontendNewsitem>();
		mainContent.add(newsitem);
		mv.put("main_content", mainContent);
		
		view.render(mv, request, response);		
		final String content = response.getContentAsString();
		
		assertTrue(content.contains("\"date\":\"24 Apr 2009\""));
	}
	
	@Test
	public void testShouldCorrectlyFormatGeotagInformation() throws Exception {
		FrontendNewsitemImpl newsitem = new FrontendNewsitemImpl();
		newsitem.setName("Title");
		newsitem.setDate(new DateTime(2009, 4, 24, 0, 0, 0, 0).toDate());
		Geocode geocode = new Geocode("Somewhere", -51.2, 1.2);
		newsitem.setGeocode(geocode);
		
		List<FrontendNewsitemImpl> mainContent = new ArrayList<FrontendNewsitemImpl>();
		mainContent.add(newsitem);
		mv.put("main_content", mainContent);
		
		view.render(mv, request, response);
		
		final String content = response.getContentAsString();		
		assertTrue(content.contains("\"latitude\":-51.2"));
		assertTrue(content.contains("\"longitude\":1.2"));
	}
	
}
