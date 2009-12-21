package nz.co.searchwellington.model;

public class Supression {

    private int id;
    private String url;
    
    public Supression() {        
    }
      
    public Supression(String urlToSupress) {
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
