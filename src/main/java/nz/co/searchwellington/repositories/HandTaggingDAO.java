package nz.co.searchwellington.repositories;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.taggingvotes.HandTagging;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class HandTaggingDAO {
	
	private static Logger log = Logger.getLogger(HandTaggingDAO.class);
	
	private SessionFactory sessionFactory;
		
	public HandTaggingDAO() {
	}
	
	@Autowired
	public HandTaggingDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@SuppressWarnings("unchecked")
	public List<HandTagging> getHandTaggingsForResource(Resource resource) {
		List<HandTagging> handTaggings = sessionFactory.getCurrentSession().createCriteria(HandTagging.class).
			add(Restrictions.eq("resource", resource)).
			setCacheable(true).
			list();
		return handTaggings;
	}
	
	@Transactional
	public void delete(HandTagging handTagging) {
		sessionFactory.getCurrentSession().delete(handTagging);
	}
		
    public Set<Tag> getHandpickedTagsForThisResourceByUser(User user, Resource resource) { // TODO stop gap measure until editor understands votes
		Set<Tag>tags = new HashSet<Tag>();		
		if (user == null) {
			return tags;
		}
		List<HandTagging> handTaggings = getHandTaggingsForResourceByUser(resource, user);				
		for (HandTagging tagging : handTaggings) {
			tags.add(tagging.getTag());
		}
		return tags;
	}
    
    @SuppressWarnings("unchecked")
	public List<HandTagging> getVotesForTag(Tag tag) {
    	List<HandTagging> handTaggings = sessionFactory.getCurrentSession().createCriteria(HandTagging.class).
		add(Restrictions.eq("tag", tag)).
		setCacheable(true).
		list();
    	return handTaggings;
    }
    
	@Transactional
	public void setUsersTagVotesForResource(Resource editResource, User user, Set<Tag> tags) {
		this.clearTagsForResourceByUser(editResource, user);
		for (Tag tag : tags) {
			this.addTag(user, tag, editResource);				
		}		
	}
	
	@Transactional
    public void addTag(User user, Tag tag, Resource resource) {
		Set<Tag> existingVotes = this.getHandpickedTagsForThisResourceByUser(user, resource);
		if (!existingVotes.contains(tag)) {			
			HandTagging newTagging = new HandTagging(0, resource, user, tag);
				log.info("Adding new hand tagging: " + newTagging);
				sessionFactory.getCurrentSession().save(newTagging);
		}
	}
	
	public void clearTags(Resource resource) {
		for (HandTagging handTagging : this.getHandTaggingsForResource(resource)) {
			sessionFactory.getCurrentSession().delete(handTagging);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<HandTagging> getUsersVotes(User user) {
		return sessionFactory.getCurrentSession().createCriteria(HandTagging.class).
		add(Restrictions.eq("user", user)).
		setCacheable(true).
		list();
	}
	
	private void clearTagsForResourceByUser(Resource resource, User user) {
		for (HandTagging handTagging : this.getHandTaggingsForResourceByUser(resource, user)) {
			sessionFactory.getCurrentSession().delete(handTagging);
		}
	}
	
	@SuppressWarnings("unchecked")
	private List<HandTagging> getHandTaggingsForResourceByUser(Resource resource, User user) {
		List<HandTagging> handTaggings = sessionFactory.getCurrentSession().createCriteria(HandTagging.class).
			add(Restrictions.eq("resource", resource)).
			add(Restrictions.eq("user", user)).
			setCacheable(true).
			list();
		return handTaggings;
	}
	
}
