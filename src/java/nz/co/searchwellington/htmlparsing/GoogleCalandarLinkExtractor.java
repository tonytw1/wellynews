package nz.co.searchwellington.htmlparsing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.LinkRegexFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class GoogleCalandarLinkExtractor implements Extractor {

    Logger log = Logger.getLogger(GoogleCalandarLinkExtractor.class);
    
    
    protected boolean isValid(String url) {
        if (url == null) {
            return false;
        }
        return url.matches("http://www.google.com/calendar/render\\?.*");       
    }
	
    
	public List<String> extractLinks(String inputHTML) {
		List<String> links = new ArrayList<String>();

		Parser parser = new Parser();
		try {
			parser.setInputHTML(inputHTML);

			NodeFilter googleLinksFilter = new LinkRegexFilter("http://www.google.com/calendar/render", true);
			NodeList googleLinks = parser.extractAllNodesThatMatch(googleLinksFilter);
			log.info("Found " + googleLinks.size() + " possible Google Calender links.");

			for (int i = 0; i < googleLinks.size(); i++) {
				Tag tag = (Tag) googleLinks.elementAt(i);
				final String feedLink = tag.getAttribute("href");
                if (isValid(feedLink)) {
                    links.add(feedLink);
                }
			}
			
		} catch (ParserException e) {
			log.warn("Parser exception while trying to extract google calendar links.");
		}
		return links;
	}

}
