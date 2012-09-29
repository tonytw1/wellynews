package nz.co.searchwellington.repositories;

import nz.co.searchwellington.model.Supression;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SupressionDAO implements SupressionRepository {
    
	private static Logger log = Logger.getLogger(SupressionDAO.class);
	
    private SessionFactory sessionFactory;
    
    @Autowired
    public SupressionDAO() {
	}
    
	public SupressionDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
	
    @Transactional
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
    
    @Transactional
    public void removeSupressionForUrl(String url) {
        Supression existingSupression = (Supression) sessionFactory.getCurrentSession().createCriteria(Supression.class).add(Expression.eq("url", url)).setMaxResults(1).uniqueResult();
        if (existingSupression != null) {
            sessionFactory.getCurrentSession().delete(existingSupression);
        }
    }
    
    @Transactional
    private Supression createSupression(String urlToSupress) {
    	if (urlToSupress != null) {
    		return new Supression(urlToSupress);
    	}
    	return null;
    }
    
}
