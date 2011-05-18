package nz.co.searchwellington.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Resource;

import org.joda.time.DateTime;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JSONViewTest extends TestCase {
	
	MockHttpServletRequest request;
	MockHttpServletResponse response;
	Map<String, Object> mv;
	JSONView view;
	
	@Override
	protected void setUp() throws Exception {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		mv = new HashMap<String, Object>();
		view = new JSONView();
	}
	
	public void testShouldCorrectlyFormatDateField() throws Exception {		
		Newsitem newsitem = new NewsitemImpl();
		newsitem.setName("Title");
		newsitem.setUrl("http://url");
		newsitem.setDate(new DateTime(2009, 4, 24, 0, 0, 0, 0).toDate());
		
		List<Resource> mainContent = new ArrayList<Resource>();
		mainContent.add(newsitem);
		mv.put("main_content", mainContent);
		
		view.render(mv, request, response);		
		final String content = response.getContentAsString();		
		assertTrue(content.contains("\"date\": \"24 Apr 2009\""));
	}
	
	
	
	public void testShouldCorrectlyFormatGeotagInformation() throws Exception {
		Newsitem newsitem = new NewsitemImpl();
		newsitem.setName("Title");
		newsitem.setDate(new DateTime(2009, 4, 24, 0, 0, 0, 0).toDate());
		Geocode geocode = new Geocode("Somewhere", -51.2, 1.2);
		newsitem.setGeocode(geocode);
		
		List<Resource> mainContent = new ArrayList<Resource>();
		mainContent.add(newsitem);
		mv.put("main_content", mainContent);
		
		view.render(mv, request, response);
		
		final String content = response.getContentAsString();		
		assertTrue(content.contains("\"latitude\": -51.2"));
		assertTrue(content.contains("\"longitude\": 1.2"));
	}
	
}
