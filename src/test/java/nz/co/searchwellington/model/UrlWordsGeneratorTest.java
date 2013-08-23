package nz.co.searchwellington.model;

import static org.junit.Assert.assertEquals;
import nz.co.searchwellington.model.frontend.FrontendNewsitemImpl;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

public class UrlWordsGeneratorTest {

	private UrlWordsGenerator urlWordsGenerator;
	
	private FrontendNewsitemImpl newsitem;
	
	@Before
	public void setup() {
		newsitem = new FrontendNewsitemImpl();
		newsitem.setName("Some thing happening");
		DateTime pubdate = new DateTime(2010, 4, 2, 0, 0, 0, 0);
		newsitem.setDate(pubdate.toDate());
		this.urlWordsGenerator = new UrlWordsGenerator();
	}
	
	@Test
	public void shouldProduceCorrectUrlBasedOnPublisherDateAndHeadline() throws Exception {		
		Website publisher = new WebsiteImpl();
		publisher.setName("Island Bay school");
		newsitem.setPublisherName("Island Bay school");
		
		assertEquals("/island-bay-school/2010/apr/2/some-thing-happening", urlWordsGenerator.makeUrlForNewsitem(newsitem));
	}
	
	@Test
	public void urlWordshouldBeDateAndHeadlineIfPublisherIsNotSet() throws Exception {
		assertEquals("/2010/apr/2/some-thing-happening", urlWordsGenerator.makeUrlForNewsitem(newsitem));
	}
	
}
