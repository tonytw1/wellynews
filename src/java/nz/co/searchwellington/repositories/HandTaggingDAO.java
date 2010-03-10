package nz.co.searchwellington.repositories;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nz.co.searchwellington.model.HandTagging;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;

public class HandTaggingDAO {

	
	SessionFactory sessionFactory;

	
    public HandTaggingDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public Set<Tag> getHandpickedTagsForThisResourceByUser(User loggedInUser, Resource resource) {
		Set<Tag>tags = new HashSet<Tag>();
		
		List<HandTagging> handTaggings = sessionFactory.getCurrentSession().createCriteria(HandTagging.class).
			add(Expression.eq("resource", resource)).	// TODO user
			setCacheable(true).
			list();
				
		for (HandTagging tagging : handTaggings) {
			tags.add(tagging.getTag());
		}
		return tags;
	}

	public void addTag(User loggedInUser, Tag tag, Resource resource) {
		// TODO Auto-generated method stub		
	}

	public Set<Tag> getHandTagsForResource(Resource resource) {
		return new HashSet<Tag>();
	}

	public void setTags(Resource editResource, Object object, Set<Tag> tags) {
		// TODO Auto-generated method stub
		
	}

	public void clearTags(Resource editResource, Object object) {
		// TODO Auto-generated method stub
		
	}

}
