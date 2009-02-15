package nz.co.searchwellington.model.decoraters.editing;

import nz.co.searchwellington.model.PublishedResource;
import nz.co.searchwellington.model.Website;

public class EditablePublishedResourceWrapper extends EditableResourceWrapper implements PublishedResource {
    
    PublishedResource resource;
    
    public EditablePublishedResourceWrapper(PublishedResource resource) {
        super(resource);
        this.resource = resource;
    }

    final public Website getPublisher() {
        return resource.getPublisher();
    }

    final public void setPublisher(Website publisher) {
       resource.setPublisher(publisher);
    }
    
}
