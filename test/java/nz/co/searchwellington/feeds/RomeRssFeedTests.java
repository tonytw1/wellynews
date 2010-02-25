package nz.co.searchwellington.feeds;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.RssFeedable;
import nz.co.searchwellington.views.RomeRssFeed;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

public class RomeRssFeedTests extends TestCase {

	
	public void testShouldRenderRss() throws Exception { 
		Newsitem newsitem = new NewsitemImpl();
		newsitem.setName("test item");
	
		List<Newsitem> content = new ArrayList<Newsitem>();
		content.add(newsitem);
		Document document = createOutputAndParseBackDocument(newsitem, content);
		
        List items = document.selectNodes( "//rss/channel/item" );
	    assertEquals(1, items.size());
	}
		
	public void testShouldRenderGeocodeInRss() throws Exception { 
		Newsitem newsitem = new NewsitemImpl();
		newsitem.setName("test item");		
		Geocode geocode = new Geocode("119 Farringdon Road, London");
        geocode.setLatitude(3);
        geocode.setLongitude(45);
		newsitem.setGeocode(geocode);
		
		List<Newsitem> content = new ArrayList<Newsitem>();
		content.add(newsitem);
		Document document = createOutputAndParseBackDocument(newsitem, content);

		List items = document.selectNodes( "//rss/channel/item/geo:lat" );
	    assertEquals(1, items.size());        
        items = document.selectNodes( "//rss/channel/item/geo:long" );
        assertEquals(1, items.size());
	}

	
	private Document createOutputAndParseBackDocument(Newsitem newsitem, List<Newsitem> content) throws DocumentException {
		List<RssFeedable> rssContent = new ArrayList<RssFeedable>();
		rssContent.addAll((Collection<? extends RssFeedable>) content);
		RomeRssFeed feed = new RomeRssFeed("a", "b", "c", rssContent);
		String output = feed.outputAsXml();
        Document document = parse(output);
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
