
package nz.co.searchwellington.repositories;

import nz.co.searchwellington.config.Config;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

public class ConfigDAO implements ConfigRepository {

    
    Logger log = Logger.getLogger(ConfigDAO.class);
    
    SessionFactory sessionFactory;


    public ConfigDAO(SessionFactory sessionFactory) {
        super();
        this.sessionFactory = sessionFactory;
    }
    
    
    public Config getConfig() {
        Config config  = loadConfigObject(); 
        return config;
    }
    
    
    public void saveConfig(Config config) {
        sessionFactory.getCurrentSession().saveOrUpdate(config);        
    }
    
       
    public String getStatsTracking() {
        Config config  = loadConfigObject();            
        if (config != null) {
            log.debug("Loaded Config object from database.");
            return config.getStatsTracking();
        } else {
            log.warn("Failed to load Config object from database.");
        }
        return null;
    }
    
    
   


    public String getFlickrPoolGroupId() {
        Config config  = loadConfigObject();  
        if (config != null) {
            log.debug("Loaded Config object from database.");
            return config.getFlickrPoolGroupId();
        } else {
            log.warn("Failed to load Config object from database.");
        }
        return null;
    }

    
    
    public boolean getUseClickThroughCounter() {
        Config config  = loadConfigObject();     
        if (config != null) {
            // TODO do a proper hibernate boolean mapping.
            log.debug("Loaded Config object from database.");
            if (config.getUseClickthroughCounter() != null && config.getUseClickthroughCounter().equals("1")) {
                return true;
            }
        } else {
            log.warn("Failed to load Config object from database.");
        }
        return false;
    }
    
    
    public boolean isTwitterListenerEnabled() {
    	 Config config  = loadConfigObject();  
         if (config != null) {
        	 return config.isTwitterListenerEnabled();
         }
         return false;
    }
    
    
    
    private Config loadConfigObject() {
        return  (Config) sessionFactory.getCurrentSession().createCriteria(Config.class).setCacheable(true).uniqueResult();
    }
    
    
}

