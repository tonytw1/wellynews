package nz.co.searchwellington.filters.attributesetters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.HibernateResourceDAO;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;

public class PublisherPageAttributeSetterTest {
	
	@Mock HibernateResourceDAO resourceDAO;
	@Mock Website publisher;
	private MockHttpServletRequest request;
	private PublisherPageAttributeSetter pageAttributeSetter;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		request = new MockHttpServletRequest();
		when(publisher.getName()).thenReturn("Wellington City Council");
		when(resourceDAO.getPublisherByUrlWords("wellington-city-council")).thenReturn(publisher);
		pageAttributeSetter = new PublisherPageAttributeSetter(resourceDAO);		
	}
	
	@Test
	public void shouldSetPublisherAttributeForPublisherPath() {
		request.setPathInfo("/wellington-city-council");

		pageAttributeSetter.setAttributes(request);
		
		assertEquals(publisher, request.getAttribute("publisher"));		
	}
	
	@Test
	public void shouldSetPublisherAttributeForPublisherEditPath() {
		request.setPathInfo("/wellington-city-council/edit");
		
		pageAttributeSetter.setAttributes(request);
		
		assertEquals(publisher, request.getAttribute("publisher"));		
	}
	
	@Test
	public void shouldSetPublisherAttributeForPublisherRssPath() {
		request.setPathInfo("/wellington-city-council/rss");
		
		pageAttributeSetter.setAttributes(request);
		
		assertEquals(publisher, request.getAttribute("publisher"));		
	}
	
}
