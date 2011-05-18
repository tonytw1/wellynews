package nz.co.searchwellington.repositories;

import java.util.Iterator;
import java.util.List;

import nz.co.searchwellington.model.User;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.springframework.transaction.annotation.Transactional;

public class HibernateBackedUserDAO implements UserRepository {

    private SessionFactory sessionFactory;
    
    public HibernateBackedUserDAO() {
    }
    
	public HibernateBackedUserDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
          
    public User getUserByOpenId(String openId) {
    	return (User) sessionFactory.getCurrentSession().createCriteria(User.class).add(Expression.eq("openId", openId)).uniqueResult();     
    }
    
    @SuppressWarnings("unchecked")
	public List<User> getActiveUsers() {
		return sessionFactory.getCurrentSession().createCriteria(User.class).
			addOrder(Order.asc("profilename")).
			setCacheable(true).
			list();
	}
    
    @Transactional
	public void saveUser(User user) {     
        sessionFactory.getCurrentSession().saveOrUpdate(user);
    }

	public User getUserByProfileName(String profilename) {
	  	return (User) sessionFactory.getCurrentSession().createCriteria(User.class).add(Expression.eq("profilename", profilename)).uniqueResult();     	    
	}
	
	@Override
	public User getUserByTwitterId(int twitterId) {
	  	return (User) sessionFactory.getCurrentSession().createCriteria(User.class).add(Expression.eq("twitterId", twitterId)).uniqueResult();     	    
	}
	
	public User getUserByApiKey(String apiKey) {
	  	return (User) sessionFactory.getCurrentSession().createCriteria(User.class).add(Expression.eq("apikey", apiKey)).uniqueResult();     	    
	}

	// TODO fails of no users
	public int getNextAvailableAnonUserNumber() {
		Iterator iterate = sessionFactory.getCurrentSession().iterate("select max(id) from UserImpl");
		if (iterate != null && iterate.hasNext()) {
			Integer next = (Integer) iterate.next();
			if (next != null) {
				return next + 1;
			}
		}
		return 1;
	}

	@Override
	public void deleteUser(User anonUser) {
		// TODO Auto-generated method stub
		
	}
	
}

