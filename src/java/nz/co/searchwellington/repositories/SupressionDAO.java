package nz.co.searchwellington.repositories;

import nz.co.searchwellington.model.Supression;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;


public class SupressionDAO implements SupressionRepository {
    
    Logger log = Logger.getLogger(SupressionDAO.class);
    
    
    private SessionFactory sessionFactory;
    
    
    public SupressionDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
        
    public void addSuppression(String urlToSuppress) {
    	Supression suppression = createSupression(urlToSuppress);
    	if (suppression != null) {
    		sessionFactory.getCurrentSession().saveOrUpdate(suppression);
    		log.info("Created suppression for: " + suppression.getUrl());
    	}
    }


    public boolean isSupressed(String url) {
        Supression existingSupression = (Supression) sessionFactory.getCurrentSession().createCriteria(Supression.class).add(Expression.eq("url", url)).setMaxResults(1).uniqueResult(); 
        if (existingSupression != null) {
            return true;
        }        
        return false;
    }


    public void removeSupressionForUrl(String url) {
        Supression existingSupression = (Supression) sessionFactory.getCurrentSession().createCriteria(Supression.class).add(Expression.eq("url", url)).setMaxResults(1).uniqueResult();
        if (existingSupression != null) {
            sessionFactory.getCurrentSession().delete(existingSupression);
        }
    }

    private Supression createSupression(String urlToSupress) {
    	if (urlToSupress != null) {
    		return new Supression(urlToSupress);
    	}
    	return null;
    }
}
