package nz.co.searchwellington.repositories;

import nz.co.searchwellington.model.Supression;



public interface SupressionRepository {
         
    public void removeSupressionForUrl(String url);
    
    public boolean isSupressed(String url);

    public void addSupression(Supression supression);

    public Supression createSupression(String urlToSupress);
  
}
