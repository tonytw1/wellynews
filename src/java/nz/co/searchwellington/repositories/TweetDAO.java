package nz.co.searchwellington.repositories;

import java.util.List;

import nz.co.searchwellington.model.Twit;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;

public class TweetDAO {

	SessionFactory sessionFactory;

	public TweetDAO() {
	}

	public TweetDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public Twit loadTweetByTwitterId(Long twitterId) {
		return (Twit) sessionFactory.getCurrentSession().createCriteria(
				Twit.class).add(Expression.eq("twitterid", twitterId))
				.setMaxResults(1).setCacheable(true).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public List<Twit> getAllTweets() {
		return sessionFactory.getCurrentSession().createCriteria(Twit.class)
				.list();
	}

	public void saveTwit(Twit twit) {
		sessionFactory.getCurrentSession().saveOrUpdate(twit);
		sessionFactory.getCurrentSession().flush();
	}

}
