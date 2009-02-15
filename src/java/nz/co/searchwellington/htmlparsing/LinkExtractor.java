package nz.co.searchwellington.htmlparsing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class LinkExtractor implements Extractor {

    Logger log = Logger.getLogger(LinkExtractor.class);
    
    public List<String> extractLinks(String inputHTML) {
        List<String> links = new ArrayList<String>();
        
        // TODO use injection for the extractor chain.
        log.info("Looking for rss links.");
        Extractor rssLinkExtractor = new RssLinkExtractor();
        List<String> rssLinks = rssLinkExtractor.extractLinks(inputHTML);
        log.info("Found " + rssLinks.size() + " rss links.");
        links.addAll(rssLinks);
        
        log.info("Looking for google calendar links.");
        Extractor googleCalandarLinkExtractor = new GoogleCalandarLinkExtractor();
        List<String> googleCalendarLinks = googleCalandarLinkExtractor.extractLinks(inputHTML);                            
        log.info("Found " + googleCalendarLinks.size() + " google calendar links.");
        links.addAll(googleCalendarLinks);
        
        log.debug("Looking for ical events links.");
        Extractor icalEventLinkExtractor = new IcalEventsLinkExtractor();
        List<String> icalEventLinks = icalEventLinkExtractor.extractLinks(inputHTML);
        log.info("Found " + icalEventLinks.size() + " ical event links.");
        links.addAll(icalEventLinks);
        
        return links;
    }
    
    
    

}
