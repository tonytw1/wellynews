package nz.co.searchwellington.repositories;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nz.co.searchwellington.model.HandTagging;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;

public class HandTaggingDAO {
	
	static Logger log = Logger.getLogger(HandTaggingDAO.class);
	
	SessionFactory sessionFactory;
	
    public HandTaggingDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

    
	public List<HandTagging> getHandTaggingsForResource(Resource resource) {
		List<HandTagging> handTaggings = sessionFactory.getCurrentSession().createCriteria(HandTagging.class).
			add(Expression.eq("resource", resource)).
			setCacheable(true).
			list();
		return handTaggings;
	}
	
	
    public Set<Tag> getHandpickedTagsForThisResourceByUser(User user, Resource resource) { // TODO stop gap measure until editor understands votes
		Set<Tag>tags = new HashSet<Tag>();		
		List<HandTagging> handTaggings = getHandTaggings(resource, user);				
		for (HandTagging tagging : handTaggings) {
			tags.add(tagging.getTag());
		}
		return tags;
	}


	private List<HandTagging> getHandTaggings(Resource resource, User user) {
		List<HandTagging> handTaggings = sessionFactory.getCurrentSession().createCriteria(HandTagging.class).
			add(Expression.eq("resource", resource)).
			add(Expression.eq("user", user)).
			setCacheable(true).
			list();
		return handTaggings;
	}

	
    public void addTag(User user, Tag tag, Resource resource) {
		HandTagging existing = (HandTagging) sessionFactory.getCurrentSession().createCriteria(HandTagging.class).
			add(Expression.eq("resource", resource)).
			add(Expression.eq("user", user)).
			add(Expression.eq("tag", tag)).
			setCacheable(true).uniqueResult();

		if (existing == null) {
			HandTagging newTagging = new HandTagging(0, resource, user, tag);
			log.info("Adding new hand tagging: " + newTagging);
			sessionFactory.getCurrentSession().save(newTagging);
		}
		sessionFactory.getCurrentSession().flush();
	}

    
    public void clearTags(Resource resource, User user) {
		for (HandTagging handTagging : this.getHandTaggings(resource, user)) {
			sessionFactory.getCurrentSession().delete(handTagging);
		}
		sessionFactory.getCurrentSession().flush();
	}


	public void clearTags(Resource resource) {
		for (HandTagging handTagging : this.getHandTaggingsForResource(resource)) {
			sessionFactory.getCurrentSession().delete(handTagging);
		}
		sessionFactory.getCurrentSession().flush();
	}

}
