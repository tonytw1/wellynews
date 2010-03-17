package nz.co.searchwellington.repositories;

import java.util.Date;
import java.util.List;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Suggestion;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;

public class SuggestionDAO {
	
	Logger log = Logger.getLogger(SuggestionDAO.class);
	    
	private SessionFactory sessionFactory;
	    	
	
	public SuggestionDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	
	public Suggestion createSuggestion(Feed feed, String url, Date firstSeen) {
		return new Suggestion(feed, url, firstSeen);
	}
	 
	 
	public void addSuggestion(Suggestion suggestion) {
		log.info("Creating suggestion for: " + suggestion.getUrl());        
		sessionFactory.getCurrentSession().saveOrUpdate(suggestion);
	}


	public boolean isSuggested(String url) {
		Suggestion existingSuggestion = (Suggestion) sessionFactory.getCurrentSession().createCriteria(Suggestion.class).
	      	add(Expression.eq("url", url)).
	      	setMaxResults(1).
	        uniqueResult(); 
		 
		 if (existingSuggestion != null) {
			 return true;
		 }        
		 return false;
	 }
	 
	 	
	 public List<Suggestion> getAllSuggestions() {        
		 return getSuggestions(500); 
	 }
	 
	 
	 // TODO This should really be private or in a wrapping service - ping ponging between this, the caller and getFeedNewsitems
	 @SuppressWarnings("unchecked")
	 public List<Suggestion> getSuggestions(int maxResults) {
		 return sessionFactory.getCurrentSession().createCriteria(Suggestion.class).
	        addOrder(Order.desc("firstSeen")).
	        setCacheable(true).
	        setMaxResults(maxResults).
	        list();
	 }

	 
	public void removeSuggestion(String url) {
		Suggestion existingsuggestion = (Suggestion) sessionFactory.getCurrentSession().createCriteria(Suggestion.class).add(Expression.eq("url", url)).setMaxResults(1).uniqueResult();
		if (existingsuggestion != null) {
			sessionFactory.getCurrentSession().delete(existingsuggestion);
		}
	}
	
}
