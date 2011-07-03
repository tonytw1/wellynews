package nz.co.searchwellington.views;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;
import nz.co.searchwellington.model.frontend.FrontendNewsitemImpl;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JSONViewTest {
	
	MockHttpServletRequest request;
	MockHttpServletResponse response;
	Map<String, Object> mv;
	JSONView view;
	
	@Before
	public void setUp() throws Exception {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		mv = new HashMap<String, Object>();
		view = new JSONView();
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
		assertTrue(content.contains("\"date\": \"24 Apr 2009\""));
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
		assertTrue(content.contains("\"latitude\": -51.2"));
		assertTrue(content.contains("\"longitude\": 1.2"));
	}
	
}
