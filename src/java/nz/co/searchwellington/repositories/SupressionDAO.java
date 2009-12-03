package nz.co.searchwellington.repositories;

import nz.co.searchwellington.model.Supression;
import nz.co.searchwellington.model.SupressionImpl;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;


public class SupressionDAO implements SupressionRepository {
    
    Logger log = Logger.getLogger(SupressionDAO.class);
    
    
    private SessionFactory sessionFactory;
    
    
    public SupressionDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    
    
    
    public Supression createSupression(String urlToSupress) {
        if (urlToSupress != null) {
            return new SupressionImpl(urlToSupress);
        }
        return null;
    }





    public void addSupression(Supression supression) {
        log.info("Creating supression for: " + supression.getUrl());        
        sessionFactory.getCurrentSession().saveOrUpdate(supression);
        sessionFactory.getCurrentSession().flush();        
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
            sessionFactory.getCurrentSession().flush();
        }
    }


}
