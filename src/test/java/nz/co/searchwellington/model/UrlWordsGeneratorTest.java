package nz.co.searchwellington.model;

import org.joda.time.DateTime;

import junit.framework.TestCase;

public class UrlWordsGeneratorTest extends TestCase {

	public void testShouldProduceCorrectUrl() throws Exception {		
		Website publisher = new WebsiteImpl();
		publisher.setName("Island Bay school");

		Newsitem newsitem = new NewsitemImpl();
		DateTime pubdate = new DateTime(2010, 4, 2, 0, 0, 0, 0);
		newsitem.setDate(pubdate.toDate());
		newsitem.setPublisher(publisher);
		newsitem.setName("Some thing happening");
		
		assertEquals("/island-bay-school/2010/apr/02/some-thing-happening", UrlWordsGenerator.markUrlForNewsitem(newsitem));
	}
	
}
