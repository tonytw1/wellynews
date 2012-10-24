package nz.co.searchwellington.repositories;

import java.util.HashSet;
import java.util.List;

import nz.co.searchwellington.model.Tag;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Component
public class TagDAO {

	private SessionFactory sessionFactory;

	public TagDAO() {
	}
	
	@Autowired
	public TagDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Deprecated
	public Tag createNewTag() {
		return new Tag(0, "", "", null, new HashSet<Tag>(), 0, false, false);
	}
	
	public Tag createNewTag(String tagUrlWords, String displayName) {
		return new Tag(0, tagUrlWords, displayName, null, new HashSet<Tag>(), 0, false, false);
	}
		
	public Tag loadTagById(int tagID) {
		return (Tag) sessionFactory.getCurrentSession().get(Tag.class, tagID);
	}
	
	public Tag loadTagByName(String tagName) {
        return (Tag) sessionFactory.getCurrentSession().
        createCriteria(Tag.class).
        add(Restrictions.eq("name", tagName)).
        uniqueResult();    
    }

	@SuppressWarnings("unchecked")
	public List<Tag> getAllTags() {
		return sessionFactory.getCurrentSession().createCriteria(Tag.class).
			addOrder(Order.asc("displayName")).
			setCacheable(true).
			list();
	}

	// TODO can hiberate do a batch load?
	public List<Tag> loadTagsById(List<Integer> tagIds) {
		final List<Tag> tags = Lists.newArrayList();
		for (Integer tagId : tagIds) {
			Tag tag = this.loadTagById(tagId);
			if (tag != null) {
				tags.add(tag);
			}
		}
		return tags;
	}

	@SuppressWarnings("unchecked")
	public List<Tag> getTopLevelTags() {
		return sessionFactory.getCurrentSession().createCriteria(Tag.class)
				.add(Restrictions.isNull("parent")).addOrder(Order.asc("name"))
				.setCacheable(true).list();
	}
		
	@Transactional
	public void saveTag(Tag editTag) {
		 sessionFactory.getCurrentSession().saveOrUpdate(editTag);
		 sessionFactory.evictCollection("nz.co.searchwellington.model.Tag.children");
		 // TODO solr index needs updating if a tag moves to a new parent.
	}
	 	
	@Transactional
	public void deleteTag(Tag tag) {
		 sessionFactory.getCurrentSession().delete(tag);
	}
	
	// TODO hup to CRS
	public List<String> getTagNamesStartingWith(String q) {
		  Session session = sessionFactory.getCurrentSession();
		  return session.createQuery("select name from nz.co.searchwellington.model.Tag where name like ? order by name").setString(0, q + '%').setMaxResults(50).list();        
	}

	public List<Tag> getFeaturedTags() {
		return sessionFactory.getCurrentSession().createCriteria(Tag.class)
		.add(Restrictions.eq("featured", true)).addOrder(Order.asc("name"))
		.setCacheable(true).list();
	}
	
}
