package nz.co.searchwellington.sitemap;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

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

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class GoogleSitemapServiceTests extends TestCase {
    
    public void testShouldRenderSiteMap() throws Exception {
        
        List<Tag> tags = new ArrayList<Tag>();
        Tag apples = new Tag();
        apples.setName("apples");
        
        Tag bananas = new Tag();
        bananas.setName("bananas");
        tags.add(apples);
        tags.add(bananas);  

        
        Date today = Calendar.getInstance().getTime();        
        ResourceRepository resourceDAO = createMock(ResourceRepository.class);        
        expect(resourceDAO.getLastLiveTimeForTag(apples)).andReturn(today);
        expect(resourceDAO.getLastLiveTimeForTag(bananas)).andReturn(null);
        replay(resourceDAO);
        
        DateFormatter dateFormatter = createMock(DateFormatter.class);
        expect(dateFormatter.formatW3CDate(today)).andReturn("today");
        replay(dateFormatter);
                
        GoogleSitemapService service = new GoogleSitemapService(resourceDAO, dateFormatter);        
        final String xml = service.render(tags, "http://test");              
        assertNotNull(xml);
        System.out.println(xml);
        
        Document document = parse(xml);
        Element urlset = (Element) document.selectSingleNode("//urlset");
        assertNotNull(urlset);
     
        List locs = document.selectNodes( "//urlset/sitemap:url/sitemap:loc" );
        assertEquals(2, locs.size());
        
        // TODO Xpath to assert the lastmod tag has the correct text in it?
        List lastmods = document.selectNodes( "//urlset/sitemap:url/sitemap:lastmod" );
        assertEquals(1, lastmods.size());
    }

    
    
    private Document parse(String xml) throws DocumentException {
        Reader stringReader = new StringReader(xml);
        SAXReader reader = new SAXReader();
        Document document = reader.read(stringReader);
        return document;
    }
    
}
