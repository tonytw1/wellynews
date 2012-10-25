package nz.co.searchwellington.repositories;

import java.util.Iterator;
import java.util.List;

import nz.co.searchwellington.model.User;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class HibernateBackedUserDAO {

    private SessionFactory sessionFactory;
    
    public HibernateBackedUserDAO() {
    }
    
    @Autowired
	public HibernateBackedUserDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
          
    public User getUserByOpenId(String openId) {
    	return (User) sessionFactory.getCurrentSession().createCriteria(User.class).add(Restrictions.eq("openId", openId)).uniqueResult();     
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
	  	return (User) sessionFactory.getCurrentSession().createCriteria(User.class).add(Restrictions.eq("profilename", profilename)).uniqueResult();     	    
	}
	
	public User getUserByTwitterId(int twitterId) {
	  	return (User) sessionFactory.getCurrentSession().createCriteria(User.class).add(Restrictions.eq("twitterId", twitterId)).uniqueResult();     	    
	}
	
	public User getUserByApiKey(String apiKey) {
	  	return (User) sessionFactory.getCurrentSession().createCriteria(User.class).add(Restrictions.eq("apikey", apiKey)).uniqueResult();     	    
	}

	// TODO fails of no users
	public int getNextAvailableAnonUserNumber() {
		Iterator iterate = sessionFactory.getCurrentSession().createQuery("select max(id) from UserImpl").iterate();
		if (iterate != null && iterate.hasNext()) {
			Integer next = (Integer) iterate.next();
			if (next != null) {
				return next + 1;
			}
		}
		return 1;
	}

	public void deleteUser(User user) {
		sessionFactory.getCurrentSession().delete(user);		
	}
	
}

