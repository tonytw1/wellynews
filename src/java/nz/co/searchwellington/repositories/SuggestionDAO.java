package nz.co.searchwellington.repositories;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.Suggestion;
import nz.co.searchwellington.model.SuggestionFeednewsitem;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;

public class SuggestionDAO {
	
	Logger log = Logger.getLogger(SuggestionDAO.class);
	    
	private SessionFactory sessionFactory;
	private RssfeedNewsitemService rssfeedNewsitemService;
	    	
	
	public SuggestionDAO(SessionFactory sessionFactory, RssfeedNewsitemService rssfeedNewsitemService) {
		this.sessionFactory = sessionFactory;
		this.rssfeedNewsitemService = rssfeedNewsitemService;
	}

	 
	public Suggestion createSuggestion(Feed feed, String url, Date firstSeen) {
		return new Suggestion(feed, url, firstSeen);
	}
	 
	 
	public void addSuggestion(Suggestion suggestion) {
		log.info("Creating suggestion for: " + suggestion.getUrl());        
		sessionFactory.getCurrentSession().saveOrUpdate(suggestion);
		sessionFactory.getCurrentSession().flush();	        
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
			sessionFactory.getCurrentSession().flush();
		}
	}


	
	

	public List<Suggestion> getDecoratedSuggestions(List<Suggestion> bareSuggestions) {
		List<Suggestion> suggestions = new ArrayList<Suggestion>();
        for (Suggestion suggestion : bareSuggestions) {			
			if (suggestion.getFeed() != null) {
				FeedNewsitem feednewsitem = rssfeedNewsitemService.getFeedNewsitemByUrl(suggestion);
				if (feednewsitem != null) {
					suggestions.add(new SuggestionFeednewsitem(suggestion, feednewsitem.getName(), feednewsitem.getDate()));
				}
			}
		}
		return suggestions;
	}

	 


}
