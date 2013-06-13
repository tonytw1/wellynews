package nz.co.searchwellington.linkchecking;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.utils.UrlFilters;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.archiving.Snapshot;
import uk.co.eelpieconsulting.archiving.SnapshotArchive;

@Component
public class ContentHasChangedProcesser implements LinkCheckerProcessor {

	private static Logger log = Logger.getLogger(ContentHasChangedProcesser.class);
	
	private SnapshotArchive snapshotArchive;
	
	@Autowired
	public ContentHasChangedProcesser(SnapshotArchive snapshotArchive) {		
		this.snapshotArchive = snapshotArchive;
	}

	@Override
	public void process(Resource checkResource, String pageContent) {
		checkForChangeUsingSnapshots(checkResource, pageContent);
	}
	
	// TODO cleaning and filtering?
    private void checkForChangeUsingSnapshots(Resource checkResource, String after) {             
    	log.debug("Comparing content before and after snapshots from content change.");
    	
    	Snapshot snapshotBeforeHttpCheck = snapshotArchive.getLatestFor(checkResource.getUrl());
    	final String pageContentBeforeHttpCheck = snapshotBeforeHttpCheck != null ? snapshotBeforeHttpCheck.getBody() : null;						     		
        boolean contentChanged = contentChanged(pageContentBeforeHttpCheck, after);                   
        if (contentChanged) {
            log.info("Change in content checksum detected. Setting last changed.");
            checkResource.setLastChanged(new DateTime().toDate());         
        } else {
            log.info("No change in content detected.");
        }
    }
            
    private boolean contentChanged(String before, String after) {
        boolean contentChanged = false;
        if (before != null && after != null) {
            contentChanged = !(UrlFilters.stripHtml(after).equals(UrlFilters.stripHtml(before)));
            
        } else {
            final boolean bothAreNull = (before == null) && (after == null);           
            if (bothAreNull) {
                contentChanged = false;
            } else {
                contentChanged = true;
            }
        }
        return contentChanged;
    }
    
}
