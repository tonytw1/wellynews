package nz.co.searchwellington.sitemap;

import static org.mockito.Mockito.when;

import java.io.Reader;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import nz.co.searchwellington.dates.DateFormatter;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.repositories.TagDAO;
import nz.co.searchwellington.urls.UrlBuilder;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class GoogleSitemapServiceTests extends TestCase {
    
	@Mock DateFormatter dateFormatter;
	@Mock UrlBuilder urlBuilder;
	@Mock ContentRetrievalService contentRetrievalService;
	@Mock TagDAO tagDAO;
	
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
		
		tags = Lists.newArrayList();
		tags.add(apples);
		tags.add(bananas);  
		
		when(tagDAO.getAllTags()).thenReturn(tags);
		
		Date today = Calendar.getInstance().getTime();
		when(dateFormatter.formatW3CDate(today)).thenReturn("today");
		
		when(contentRetrievalService.getLastLiveTimeForTag(apples)).thenReturn(today);
		when(contentRetrievalService.getLastLiveTimeForTag(bananas)).thenReturn(null);
				
		when(urlBuilder.getTagUrl(apples)).thenReturn("http://apples");
		when(urlBuilder.getTagUrl(bananas)).thenReturn("http://bananas");
		
		service = new GoogleSitemapService(contentRetrievalService, dateFormatter, urlBuilder, tagDAO);        
	}
		
    public void testShouldRenderTagPagesWithLastModifiedTime() throws Exception {              
        final String xml = service.render("http://test");
		Document document = parse(xml);

        Element urlset = (Element) document.selectSingleNode("//urlset");
        assertNotNull(urlset);
     
        List<?> locs = document.selectNodes( "//urlset/sitemap:url/sitemap:loc" );
        assertEquals(2, locs.size());
        
        // TODO Xpath to assert the lastmod tag has the correct text in it?
        List<?> lastmods = document.selectNodes( "//urlset/sitemap:url/sitemap:lastmod" );
        assertEquals(1, lastmods.size());
    }
    
    public void testNotShouldRenderTagPaginations() throws Exception {
    	  when(contentRetrievalService.getTaggedNewitemsCount(apples)).thenReturn(65);
    	  when(contentRetrievalService.getTaggedNewitemsCount(bananas)).thenReturn(10);
          
    	  Document document = parse(service.render("http://test"));
    	  
    	  List<?> locs = document.selectNodes( "//urlset/sitemap:url/sitemap:loc" );
          assertEquals(2, locs.size());    	  
	}
    
    private Document parse(String xml) throws DocumentException {
        Reader stringReader = new StringReader(xml);
        SAXReader reader = new SAXReader();
        Document document = reader.read(stringReader);
        return document;
    }
    
}
