package nz.co.searchwellington.sitemap;

import java.util.Date;

import nz.co.searchwellington.dates.DateFormatter;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.repositories.TagDAO;
import nz.co.searchwellington.urls.UrlBuilder;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

public class GoogleSitemapService {
    
	private static final String NAMESPACE = "http://www.sitemaps.org/schemas/sitemap/0.9";
    
    private ContentRetrievalService contentRetrivalService;
    private DateFormatter dateFormatter;
    private UrlBuilder urlBuilder;
    private TagDAO tagDAO;
    
    public GoogleSitemapService(ContentRetrievalService contentRetrivalService, DateFormatter dateFormatter, UrlBuilder urlBuilder, TagDAO tagDAO) {        
        this.contentRetrivalService = contentRetrivalService;
        this.dateFormatter = dateFormatter;
        this.urlBuilder = urlBuilder;
        this.tagDAO = tagDAO;
    }
    
    public String render(String siteLocation) {
        Element urlset = DocumentHelper.createElement("urlset");
        urlset.addNamespace("sitemap", NAMESPACE);
        Document document = DocumentHelper.createDocument(urlset);        
        for (Tag tag : tagDAO.getAllTags()) {
            addTagUrl(urlset, tag, siteLocation);
        }
        return document.asXML();
    }
    
    private void addTagUrl(Element root, Tag tag, String siteLocation) {              
        final String url = urlBuilder.getTagUrl(tag);     
        String lastmod = null;        
        Date lastUpdated = contentRetrivalService.getLastLiveTimeForTag(tag);
        if (lastUpdated != null) {
        	lastmod = dateFormatter.formatW3CDate(lastUpdated);
        }
        addUrlElement(root, url, lastmod);       
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
