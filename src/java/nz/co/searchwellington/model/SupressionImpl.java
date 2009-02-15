package nz.co.searchwellington.model;


// Represents a supression in a url.
// Used to stop feed items been reimported after a delete.
// ie. When a normal good feed goes and publishes something crap.
public class SupressionImpl implements Supression {

    int id;
    String url;
    
    // For hibernate.
    public SupressionImpl() {        
    }
    
  
    public SupressionImpl(String urlToSupress) {
        url = urlToSupress;
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

    
    

}
