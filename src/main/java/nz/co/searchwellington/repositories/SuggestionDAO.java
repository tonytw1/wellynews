package nz.co.searchwellington.repositories;

import java.util.Date;
import java.util.List;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Suggestion;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.springframework.transaction.annotation.Transactional;

public class SuggestionDAO implements SuggestionRepository {
	
	static Logger log = Logger.getLogger(SuggestionDAO.class);
	    
	SessionFactory sessionFactory;
	    	
	
	public SuggestionDAO() {		
	}


	public SuggestionDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	
	@Transactional
	public Suggestion createSuggestion(Feed feed, String url, Date firstSeen) {
		return new Suggestion(feed, url, firstSeen);
	}
	 
	 
	@Transactional
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
	        addOrder(Order.desc("firstSeen")).		// TODO this ordering does not appear to be consistant
	        setCacheable(true).
	        setMaxResults(maxResults).
	        list();
	 }

	 
	 @Transactional
	 public void removeSuggestion(String url) {
		Suggestion existingsuggestion = (Suggestion) sessionFactory.getCurrentSession().createCriteria(Suggestion.class).add(Expression.eq("url", url)).setMaxResults(1).uniqueResult();
		if (existingsuggestion != null) {
			sessionFactory.getCurrentSession().delete(existingsuggestion);
		}
	}
	
}
