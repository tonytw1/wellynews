package nz.co.searchwellington.sitemap;

import static org.mockito.Mockito.stub;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import nz.co.searchwellington.dates.DateFormatter;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.urls.UrlBuilder;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class GoogleSitemapServiceTests extends TestCase {
    
	@Mock ResourceRepository resourceDAO;
	@Mock DateFormatter dateFormatter;
	@Mock UrlBuilder urlBuilder;
	
	List<Tag> tags;
	Tag apples;
	Tag bananas;
	
	GoogleSitemapService service;
		
	@Override
	protected void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		apples = new Tag();
		apples.setName("apples");
		
		bananas = new Tag();
		bananas.setName("bananas");
		
		tags = new ArrayList<Tag>();
		tags.add(apples);
		tags.add(bananas);  
		
		Date today = Calendar.getInstance().getTime();             
		stub(dateFormatter.formatW3CDate(today)).toReturn("today");
		
		stub(resourceDAO.getLastLiveTimeForTag(apples)).toReturn(today);
		stub(resourceDAO.getLastLiveTimeForTag(bananas)).toReturn(null);
				
		stub(urlBuilder.getTagUrl(apples)).toReturn("http://apples");
		stub(urlBuilder.getTagUrl(bananas)).toReturn("http://bananas");
		
		service = new GoogleSitemapService(resourceDAO, dateFormatter, urlBuilder);        
	}
	
	
    public void testShouldRenderTagPagesWithLastModifiedTime() throws Exception {              
        Document document = parse(service.render(tags, "http://test"));

        Element urlset = (Element) document.selectSingleNode("//urlset");
        assertNotNull(urlset);
     
        List locs = document.selectNodes( "//urlset/sitemap:url/sitemap:loc" );
        assertEquals(2, locs.size());
        
        // TODO Xpath to assert the lastmod tag has the correct text in it?
        List lastmods = document.selectNodes( "//urlset/sitemap:url/sitemap:lastmod" );
        assertEquals(1, lastmods.size());
    }
    
    
    public void testShouldRenderTagPaginations() throws Exception {
    	  stub(resourceDAO.getTaggedNewitemsCount(apples, false)).toReturn(65);
    	  stub(resourceDAO.getTaggedNewitemsCount(bananas, false)).toReturn(10);
          
    	  Document document = parse(service.render(tags, "http://test"));
    	  
    	  List locs = document.selectNodes( "//urlset/sitemap:url/sitemap:loc" );
          assertEquals(4, locs.size());    	  
	}
    
    
    private Document parse(String xml) throws DocumentException {
        Reader stringReader = new StringReader(xml);
        SAXReader reader = new SAXReader();
        Document document = reader.read(stringReader);
        return document;
    }
    
}
