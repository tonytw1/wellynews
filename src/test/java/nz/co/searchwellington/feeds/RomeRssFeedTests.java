package nz.co.searchwellington.feeds;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;
import nz.co.searchwellington.views.RomeRssFeed;
import nz.co.searchwellington.views.RssItemMaker;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import com.sun.syndication.feed.synd.SyndEntry;


public class RomeRssFeedTests extends TestCase {
	
	Newsitem newsitem;
		
	public void setUp() throws Exception {	
		newsitem = new NewsitemImpl();
		newsitem.setName("test item");
		newsitem.setDescription("test");
	}
		
	public void testShouldRenderRssItem() throws Exception {
		List<FrontendNewsitem> content = new ArrayList<FrontendNewsitem>();
		content.add(newsitem);
		Document document = createOutputAndParseBackDocument(content);
		
		List<?> items = document.selectNodes( "//rss/channel/item" );
		assertEquals(1, items.size());
	}
	
	public void testShouldRenderGeocodeInRss() throws Exception {
		Geocode geocode = new Geocode("119 Farringdon Road, London");
        geocode.setLatitude(3);
        geocode.setLongitude(45);
		newsitem.setGeocode(geocode);
		
		List<FrontendNewsitem> content = new ArrayList<FrontendNewsitem>();
		content.add(newsitem);
		Document document = createOutputAndParseBackDocument(content);

		List<?> items = document.selectNodes( "//rss/channel/item/geo:lat" );
	    assertEquals(1, items.size());        
        items = document.selectNodes( "//rss/channel/item/geo:long" );
        assertEquals(1, items.size());
	}
	
	private Document createOutputAndParseBackDocument(List<FrontendNewsitem> content) throws DocumentException {
		List<SyndEntry> rssContent = new ArrayList<SyndEntry>();		
		RssItemMaker rssItemMaker = new RssItemMaker();	// TODO this in in the wrong place implies this should be part of RssView
		for (FrontendNewsitem newsitem: content) {
			rssContent.add(rssItemMaker.getNewsitemRssItem(newsitem));
		}
		RomeRssFeed feed = new RomeRssFeed("a", "b", "c", rssContent);
		String xml = feed.outputAsXml();
        Document document = parse(xml);
		return document;
	}
	
	// TODO duplicated with the google site map tests.
	private Document parse(String xml) throws DocumentException {
		Reader stringReader = new StringReader(xml);
		SAXReader reader = new SAXReader();
		Document document = reader.read(stringReader);
		return document;
	}
	
}
