package nz.co.searchwellington.linkchecking;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.SnapshotDAO;

public class ContentHasChangedProcesser implements LinkCheckerProcessor {

	private static Logger log = Logger.getLogger(ContentHasChangedProcesser.class);
	
	private SnapshotDAO snapshotDAO;

	
	public ContentHasChangedProcesser(SnapshotDAO snapshotDAO) {		
		this.snapshotDAO = snapshotDAO;
	}


	@Override
	public void process(Resource checkResource, String pageContent) {
		checkForChangeUsingSnapshots(checkResource, pageContent);
	}
	
	// TODO cleaning and filtering?
    private void checkForChangeUsingSnapshots(Resource checkResource, String after) {             
    	log.debug("Comparing content before and after snapshots from content change.");
    	
    	final String pageContentBeforeHttpCheck = snapshotDAO.loadContentForUrl(checkResource.getUrl());									     		
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
            contentChanged = !after.equals(before);
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
