package nz.co.searchwellington.sitemap;

import java.util.Date;
import java.util.List;

import nz.co.searchwellington.dates.DateFormatter;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

public class GoogleSitemapService {
    
    private static final int MAX_NEWSITEMS = 30;	// TODO duplicated - move to a paginator class

	Logger log = Logger.getLogger(GoogleSitemapService.class);
    
    
    private static final String NAMESPACE = "http://www.sitemaps.org/schemas/sitemap/0.9";
    private ResourceRepository resourceDAO;
    private DateFormatter dateFormatter;
    private UrlBuilder urlBuilder;
    

    public GoogleSitemapService(ResourceRepository resourceDAO, DateFormatter dateFormatter, UrlBuilder urlBuilder) {        
        this.resourceDAO = resourceDAO;
        this.dateFormatter = dateFormatter;
        this.urlBuilder = urlBuilder;
    }
    
    
    public String render(List<Tag> tags, String siteLocation) {
        Element urlset = DocumentHelper.createElement("urlset");
        urlset.addNamespace("sitemap", NAMESPACE);
        Document document = DocumentHelper.createDocument(urlset);        
        for (Tag tag : tags) {
            addTagUrl(urlset, tag, siteLocation);
        }
        return document.asXML();
    }

    
    private void addTagUrl(Element root, Tag tag, String siteLocation) {      
        
        final String url = urlBuilder.getTagUrl(tag);     
        String lastmod = null;        
        Date lastUpdated = resourceDAO.getLastLiveTimeForTag(tag);
        if (lastUpdated != null) {
        	lastmod = dateFormatter.formatW3CDate(lastUpdated);
        }        
        addUrlElement(root, url, lastmod);
        
        
        int tagNewsitemCount = resourceDAO.getTaggedNewitemsCount(tag, false);
        for (int i = 2; ((i -1)  * MAX_NEWSITEMS) <= tagNewsitemCount; i =i + 1) {
        	 addUrlElement(root, url + "?page=" + i, null);	
        }
    }


	private void addUrlElement(Element root, final String url, final String dateString) {
		Element tagElement = root.addElement(new QName("url", new Namespace("sitemap", NAMESPACE)));   
        tagElement.addNamespace("sitemap", NAMESPACE);

        Element locElement = tagElement.addElement(new QName("loc", new Namespace("sitemap", NAMESPACE)));
		locElement.setText(url);
        
        if (dateString != null) {
        	Element lastmod = tagElement.addElement(new QName("lastmod", new Namespace("sitemap", NAMESPACE)));
        	lastmod.setText(dateString);
        }
	}

}
