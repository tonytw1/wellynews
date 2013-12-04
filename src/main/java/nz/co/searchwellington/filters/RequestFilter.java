package nz.co.searchwellington.filters;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.filters.attributesetters.AttributeSetter;
import nz.co.searchwellington.filters.attributesetters.CombinerPageAttributeSetter;
import nz.co.searchwellington.filters.attributesetters.FeedAttributeSetter;
import nz.co.searchwellington.filters.attributesetters.PublisherPageAttributeSetter;
import nz.co.searchwellington.filters.attributesetters.TagPageAttributeSetter;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Component("requestFilter")
@Scope(value="request", proxyMode=ScopedProxyMode.TARGET_CLASS)
public class RequestFilter {
	
	private static Logger log = Logger.getLogger(RequestFilter.class);
	 
	private RequestAttributeFilter[] filters;
	private List<AttributeSetter> attributeSetters;

	public RequestFilter() {         
    }
    
	@Autowired
    public RequestFilter(CombinerPageAttributeSetter combinerPageAttributeSetter, 
    		PublisherPageAttributeSetter publisherPageAttributeSetter,
    		FeedAttributeSetter feedAttributeSetter,
    		TagPageAttributeSetter tagPageAttibuteSetter,
    		RequestAttributeFilter[] filters) {
        
		this.attributeSetters = Lists.newArrayList(tagPageAttibuteSetter, publisherPageAttributeSetter, feedAttributeSetter, combinerPageAttributeSetter);        
        this.filters = filters;
    }
    
	public void loadAttributesOntoRequest(HttpServletRequest request) {		
    	for (RequestAttributeFilter filter : filters) {
			filter.filter(request);
		}
    	
    	if (isReservedPath(request.getPathInfo())) {
    		return;
    	}
    	
    	for (AttributeSetter attributeSetter : attributeSetters) {
    		if (attributeSetter.setAttributes(request)) {    		
    			return;
    		}
    	}
    	
		log.debug("Looking for single publisher and tag urls");       
    }
	
	public RequestAttributeFilter[] getFilters() {
		return filters;
	}

	private boolean isReservedPath(String path) {
    	Set<String> reservedUrlWords = Sets.newHashSet();		 // TODO this wants to be in the spring config
    	reservedUrlWords.add("/about");
    	reservedUrlWords.add("/api");
    	reservedUrlWords.add("/autotag");
       	reservedUrlWords.add("/index");
    	reservedUrlWords.add("/feeds");
    	reservedUrlWords.add("/comment");
    	reservedUrlWords.add("/geotagged");
    	reservedUrlWords.add("/tags");
    	
    	for(String prefix : reservedUrlWords) {
    		if (path.startsWith(prefix)) {
    			return true;
    		}
    	}    	
    	return false;
	}
	
}
