package nz.co.searchwellington.repositories;

public interface SupressionRepository {
         
    public void removeSupressionForUrl(String url);
    
    public boolean isSupressed(String url);

    public void addSuppression(String url);
  
}
