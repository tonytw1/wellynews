package nz.co.searchwellington.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Resource;

import org.joda.time.DateTime;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JSONViewTest extends TestCase {

	
	public void testShouldCorrectlyFormatDateField() throws Exception {		
		JSONView view = new JSONView();
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		Map<String, Object> mv = new HashMap<String, Object>();

		Newsitem newsitem = new NewsitemImpl();
		newsitem.setName("Title");
		newsitem.setDate(new DateTime(2009, 4, 24, 0, 0, 0, 0).toDate());
		
		List<Resource> mainContent = new ArrayList<Resource>();
		mainContent.add(newsitem);
		mv.put("main_content", mainContent);
		
		view.render(mv, request, response);
		
		final String content = response.getContentAsString();		
		assertTrue(content.contains("\"date\": \"24 Apr 2009\""));
	}
	
}
