package nz.co.searchwellington.htmlparsing;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.SnapshotDAO;

import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class SnapshotBodyExtractor {
	
    Logger log = Logger.getLogger(SnapshotBodyExtractor.class);

    private SnapshotDAO snapshotDAO;
    
    public SnapshotBodyExtractor(SnapshotDAO snapshotDAO) {	
		this.snapshotDAO = snapshotDAO;
	}

	public String extractSnapshotBodyTextFor(Resource resource) {
    	if (resource.getUrl() == null) {
    		return null;
    	}    	
    	final String content = snapshotDAO.loadLatestContentForUrl(resource.getUrl());
    	if (content != null) {
    		return extractBodyText(content);
    	}
    	return null;
    }
    
	public String extractBodyText(String htmlPage) {
		Parser parser = new Parser();
		try {
			parser.setInputHTML(htmlPage);

			NodeFilter bodyTagFilter = new TagNameFilter("BODY");
			NodeList list = parser.extractAllNodesThatMatch(bodyTagFilter);
			if (list.size() > 0) {
				Node body = list.elementAt(0);
				return body.toPlainTextString();
			}
		} catch (ParserException e) {
			log.warn("Parser exception while extracting body", e);
		}
		return null;
	}

}
