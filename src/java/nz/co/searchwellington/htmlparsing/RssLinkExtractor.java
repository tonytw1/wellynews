package nz.co.searchwellington.htmlparsing;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class RssLinkExtractor implements LinkExtractor {

    Logger log = Logger.getLogger(RssLinkExtractor.class);
        
    public Set<String> extractLinks(String inputHTML) {
        Set<String> links = new HashSet<String>();

        Parser parser = new Parser();
        try {
            
            parser.setInputHTML(inputHTML);

            NodeFilter filterNode = new AndFilter(new TagNameFilter("LINK"), new AndFilter(new HasAttributeFilter("rel"), new HasAttributeFilter("href")));
            NodeFilter filter = new AndFilter(filterNode, new NodeClassFilter(Tag.class));
            NodeList list = parser.extractAllNodesThatMatch(filter);

            for (int i = 0; i < list.size(); i++) {
                Tag tag = (Tag) list.elementAt(i);

                if (tag.getAttribute("type") != null) {
                    final boolean linkIsAFeed = tag.getAttribute("type").equals("application/rss+xml") || tag.getAttribute("type").equals("application/atom+xml");
                    if (linkIsAFeed) {
                        final String feedLink = tag.getAttribute("href");
                        log.info("Found feed url: " + feedLink);
                        links.add(feedLink);
                    }
                }
            }
            
        } catch (ParserException e) {
            log.error("ParserException will trying to extract rss links.");           
        }
        
        return links;        
    }
    
    

}
