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
          
    public User getUser(String username) {
    	return (User) sessionFactory.getCurrentSession().createCriteria(User.class).add(Expression.eq("username", username)).uniqueResult();     
    }
    
    
    public void saveUser(User user) {     
        sessionFactory.getCurrentSession().saveOrUpdate(user);
        sessionFactory.getCurrentSession().flush();       
    }

}

