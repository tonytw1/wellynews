package nz.co.searchwellington.views;

import nz.co.searchwellington.model.Resource;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;

public class RssItemMaker {
	
	public SyndEntry makeRssItem(Resource content) {
		// TODO validate - must have urls etc.
		return getDefaultRssItem(content);
	}
	
	
	private SyndEntry getDefaultRssItem(Resource content) {
        SyndEntry entry = new SyndEntryImpl();      
        entry.setTitle(stripIllegalCharacters(content.getName()));
        entry.setLink(content.getUrl());

        SyndContent description = new SyndContentImpl();
        description.setType("text/plain");
        description.setValue(stripIllegalCharacters(content.getDescription()));
        entry.setDescription(description);
        return entry;
    }

	
	private String stripIllegalCharacters(String input) {
		return input.replaceAll("[^\\u0020-\\uFFFF]", "");
	}
	
}
