package nz.co.searchwellington.htmlparsing;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

public class CompositeLinkExtractor implements LinkExtractor {

    Logger log = Logger.getLogger(CompositeLinkExtractor.class);
    
    LinkExtractor[] linkExtractors;
        
    public CompositeLinkExtractor(LinkExtractor[] linkExtractors) {
		this.linkExtractors = linkExtractors;
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
