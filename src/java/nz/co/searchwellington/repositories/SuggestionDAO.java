package nz.co.searchwellington.repositories;

import java.util.List;

import nz.co.searchwellington.model.Suggestion;
import nz.co.searchwellington.model.Supression;
import nz.co.searchwellington.model.SupressionImpl;
import nz.co.searchwellington.model.Tag;

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

	 
	 public Suggestion createSuggestion(String url) {
		 if (url != null) {
			 return new Suggestion(url);
		 }
		 return null;
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
	 
	 
	 @SuppressWarnings("unchecked")
	 public List<Suggestion> getAllSuggestions() {        
		 return sessionFactory.getCurrentSession().createCriteria(Suggestion.class).
	        addOrder(Order.desc("id")).
	        setCacheable(true).
	        list();        
	 }
	 


}
