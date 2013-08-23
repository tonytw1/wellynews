package nz.co.searchwellington.repositories;

import java.util.Date;
import java.util.List;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Suggestion;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SuggestionDAO {
	
	private static Logger log = Logger.getLogger(SuggestionDAO.class);
	    
	private SessionFactory sessionFactory;
	
	public SuggestionDAO() {		
	}

	@Autowired
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
	      	add(Restrictions.eq("url", url)).
	      	setMaxResults(1).
	        uniqueResult(); 
		 
		 if (existingSuggestion != null) {
			 return true;
		 }        
		 return false;
	 }
	
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
		Suggestion existingsuggestion = (Suggestion) sessionFactory.getCurrentSession().createCriteria(Suggestion.class).add(Restrictions.eq("url", url)).setMaxResults(1).uniqueResult();
		if (existingsuggestion != null) {
			sessionFactory.getCurrentSession().delete(existingsuggestion);
		}
	}
	
}
