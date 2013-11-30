package nz.co.searchwellington.htmlparsing;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CompositeLinkExtractor implements LinkExtractor {

    private static Logger log = Logger.getLogger(CompositeLinkExtractor.class);
    
    private LinkExtractor[] linkExtractors;
    
    @Autowired
    public CompositeLinkExtractor(LinkExtractor[] linkExtractors) {
		this.linkExtractors = linkExtractors.clone();
	}

	public Set<String> extractLinks(String inputHTML) {
        Set<String> links = new HashSet<String>();
        
        if (inputHTML != null) {        	
        	for (LinkExtractor linkExtractor : linkExtractors) {
        		Set<String> extractedLinks = linkExtractor.extractLinks(inputHTML);
        		links.addAll(extractedLinks);
			}        	
        	log.info("Found " + links + " links");
        }
               
        return links;
    }

}
