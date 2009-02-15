package nz.co.searchwellington.spam;

import nz.co.searchwellington.model.Resource;

public class SpamFilter {
    
    public boolean isSpam(Resource editResource) {
        if (editResource != null) {
            boolean urlIsSpam = editResource.getUrl() != null && editResource.getUrl().contains("rfid");
            boolean descriptionsSpam = editResource.getDescription() != null && editResource.getDescription().contains("rfid");
            return urlIsSpam || descriptionsSpam;
        }
        return false;
    }
    
}
