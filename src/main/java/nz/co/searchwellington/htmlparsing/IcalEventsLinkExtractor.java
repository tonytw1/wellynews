package nz.co.searchwellington.htmlparsing;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.LinkRegexFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.springframework.stereotype.Component;

@Component
public class IcalEventsLinkExtractor implements LinkExtractor {

	private static Logger log = Logger.getLogger(IcalEventsLinkExtractor.class);
    
    public Set<String> extractLinks(String inputHTML) {
        final Set<String> links = new HashSet<String>();

        final Parser parser = new Parser();
        try {
            parser.setInputHTML(inputHTML);
    
            NodeFilter linksFilter = new LinkRegexFilter("/event/ical", true);
            NodeList icalLinks = parser.extractAllNodesThatMatch(linksFilter);
            log.debug("Found " + icalLinks.size() + " possible ical events links.");
            for (int i = 0; i < icalLinks.size(); i++) {
                Tag tag = (Tag) icalLinks.elementAt(i);
                final String feedLink = tag.getAttribute("href");
                log.debug("Found possible ical event link: " + feedLink);
                links.add(feedLink);
            }
        } catch (ParserException e) {
            log.warn("Parser exception while trying to extract ical events calendar links.");
        }
        return links;
    }
    
}
