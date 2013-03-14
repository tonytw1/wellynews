package nz.co.searchwellington.htmlparsing;

import nz.co.searchwellington.model.Resource;

import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.archiving.Snapshot;
import uk.co.eelpieconsulting.archiving.SnapshotArchive;

@Component
public class SnapshotBodyExtractor {
	
    private static Logger log = Logger.getLogger(SnapshotBodyExtractor.class);
    
    private SnapshotArchive snapshotArchive;
    
    @Autowired
    public SnapshotBodyExtractor(SnapshotArchive snapshotArchive) {	
		this.snapshotArchive = snapshotArchive;
	}

	public String extractLatestSnapshotBodyTextFor(Resource resource) {
    	if (resource.getUrl() == null) {
    		return null;
    	}
    	
    	final Snapshot latestFor = snapshotArchive.getLatestFor(resource.getUrl());
		final String content = latestFor != null ? latestFor.getBody() : null;
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
