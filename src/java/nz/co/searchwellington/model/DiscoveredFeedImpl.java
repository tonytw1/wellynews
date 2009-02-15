package nz.co.searchwellington.model;

import java.util.Set;

public class DiscoveredFeedImpl implements DiscoveredFeed {
    
    int id;
    String url;
    Set<Resource> references;
    
    
    public DiscoveredFeedImpl() {       
    }
    
    
    
    
    public int getId() {
        return id;
    }




    public void setId(int id) {
        this.id = id;
    }


    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }




    public Set<Resource> getReferences() {
        return references;
    }




    public void setReferences(Set<Resource> references) {
        this.references = references;
    }
    
    
    
    
    
    
    

    
}
