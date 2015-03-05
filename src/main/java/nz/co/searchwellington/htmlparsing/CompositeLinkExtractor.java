package nz.co.searchwellington.htmlparsing;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CompositeLinkExtractor implements LinkExtractor {

    private LinkExtractor[] linkExtractors;
    
    @Autowired
    public CompositeLinkExtractor(LinkExtractor[] linkExtractors) {
		this.linkExtractors = linkExtractors.clone();
	}

	public Set<String> extractLinks(String inputHTML) {
        Set<String> links = Sets.newHashSet();
        if (inputHTML != null) {
        	for (LinkExtractor linkExtractor : linkExtractors) {
                links.addAll(linkExtractor.extractLinks(inputHTML));
			}
        }
        return links;
    }

}
