package nz.co.searchwellington.model.decoraters.editing;


import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.decoraters.ResourceWrapper;


public class EditableResourceWrapper extends ResourceWrapper {

    public EditableResourceWrapper(Resource resource) { 
        super(resource);  
    }
    
    
    public String getEditUrl() {
        return "/edit/edit?resource=" + resource.getId();        
    }
    
    public final String getDeleteUrl() {
        return "/edit/delete?resource=" + resource.getId();
    }
        
    public String getLinkCheckUrl() { 
        return "/admin/linkchecker/add?resource=" + resource.getId();
    }
    
    public Geocode getGeocode() {
        return resource.getGeocode();
    }

}
