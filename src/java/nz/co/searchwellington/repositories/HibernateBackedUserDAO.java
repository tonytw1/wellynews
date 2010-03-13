package nz.co.searchwellington.repositories;

import java.util.Iterator;
import java.util.List;

import nz.co.searchwellington.model.User;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;

public class HibernateBackedUserDAO implements UserRepository {

    private SessionFactory sessionFactory;
    
    public HibernateBackedUserDAO(SessionFactory sessionFactory) {
        super();
        this.sessionFactory = sessionFactory;
    }
          
    public User getUser(String username) {
    	return (User) sessionFactory.getCurrentSession().createCriteria(User.class).add(Expression.eq("username", username)).uniqueResult();     
    }
    
    @SuppressWarnings("unchecked")
	public List<User> getActiveUsers() {
		return sessionFactory.getCurrentSession().createCriteria(User.class).
			addOrder(Order.asc("profilename")).
			setCacheable(true).
			list();
	}
    
	public void saveUser(User user) {     
        sessionFactory.getCurrentSession().saveOrUpdate(user);
        sessionFactory.getCurrentSession().flush();       
    }

	public User getUserByProfileName(String profilename) {
	  	return (User) sessionFactory.getCurrentSession().createCriteria(User.class).add(Expression.eq("profilename", profilename)).uniqueResult();     	    
	}
	
	public User getUserByApiKey(String apiKey) {
	  	return (User) sessionFactory.getCurrentSession().createCriteria(User.class).add(Expression.eq("apikey", apiKey)).uniqueResult();     	    
	}

	// TODO fails of no users
	public int getNextAvailableAnonUserNumber() {
		Iterator iterate = sessionFactory.getCurrentSession().iterate("select max(id) from UserImpl");
		if (iterate != null && iterate.hasNext()) {
			return ((Integer) iterate.next()) + 1;
		}
		return 1;
	}
	
}

