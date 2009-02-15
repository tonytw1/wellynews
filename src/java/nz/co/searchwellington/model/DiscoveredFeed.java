package nz.co.searchwellington.model;

import java.util.Set;

public interface DiscoveredFeed {

    public int getId();
    public void setId(int id);

    public String getUrl();
    public void setUrl(String url);
    public Set<Resource> getReferences();
    public void setReferences(Set<Resource> references);


}