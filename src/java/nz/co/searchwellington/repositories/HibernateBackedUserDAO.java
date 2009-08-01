package nz.co.searchwellington.repositories;

import nz.co.searchwellington.model.User;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;

public class HibernateBackedUserDAO implements UserRepository {

    private SessionFactory sessionFactory;


    public HibernateBackedUserDAO(SessionFactory sessionFactory) {
        super();
        this.sessionFactory = sessionFactory;
    }
    
    
    public User getUser(String username, String password) {
        User user = (User) sessionFactory.getCurrentSession().createCriteria(User.class).add(Expression.eq("username", username)).uniqueResult();
        if (user != null && user.getPassword() != null && user.getPassword().equals(password)) {
            return user;
        }        
        return null;
    }
        
    public User getUser(String username) {
    	return (User) sessionFactory.getCurrentSession().createCriteria(User.class).add(Expression.eq("username", username)).uniqueResult();     
    }

}

