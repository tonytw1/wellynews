package nz.co.searchwellington.repositories;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import nz.co.searchwellington.model.CalendarFeed;
import nz.co.searchwellington.model.Comment;
import nz.co.searchwellington.model.CommentFeed;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedImpl;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.PublishedResource;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.ResourceImpl;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Twit;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Watchlist;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.WebsiteImpl;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

public class HibernateResourceDAO implements ResourceRepository {

    SessionFactory sessionFactory;

    
    public HibernateResourceDAO() {
    }
    
    
    public HibernateResourceDAO(SessionFactory sessionFactory) {     
        this.sessionFactory = sessionFactory;
    }
    
    public Newsitem createNewNewsitem() {         
        return new NewsitemImpl(0, "", "", "", Calendar.getInstance().getTime(), null,
                            new HashSet<DiscoveredFeed>(), null, new HashSet<Twit>());
    }
           
    public Website createNewWebsite() {
        return new WebsiteImpl( 0, "", "", Calendar.getInstance().getTime(), 
                                "", 
                                new HashSet <Feed>(), new HashSet<Watchlist>(),
                                new HashSet <DiscoveredFeed>(),
                                new HashSet <CalendarFeed>());
    }

    public Feed createNewFeed() {
        return new FeedImpl(0, "", "", "", null, null);
    }
    
    
    public Watchlist createNewWatchlist() {
        return new Watchlist(0, "", "", "", null, new HashSet <DiscoveredFeed>());
      
    }
    
    public CommentFeed createNewCommentFeed(String commentFeedUrl) {
    	return new CommentFeed(0, commentFeedUrl, new ArrayList<Comment>(), null, null, new HashSet<Resource>());
    }
        
    public CalendarFeed createNewCalendarFeed(String url) {
        return new CalendarFeed(0, url, "", "");
    }
    
    public DiscoveredFeed createNewDiscoveredFeed(String discoveredUrl) {
        DiscoveredFeed discoveredFeed = new DiscoveredFeed();
        discoveredFeed.setUrl(discoveredUrl);
        discoveredFeed.setReferences(new HashSet<Resource>());
        return discoveredFeed;
    }
    
    @SuppressWarnings("unchecked")
    public List<Integer> getAllResourceIds() {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery("select id from nz.co.searchwellington.model.ResourceImpl order by id DESC").setFetchSize(100).list();        
       
    }
        
    // TODO hup to CRS
	public List<String> getPublisherNamesByStartingLetters(String q) {
         Session session = sessionFactory.getCurrentSession();
         return session.createQuery("select name from nz.co.searchwellington.model.ResourceImpl where type='W' and name like ? order by name").setString(0, q + '%').setMaxResults(50).list();        
	}
	
	@SuppressWarnings("unchecked")
    final public List<Feed> getAllFeeds() {
        return sessionFactory.getCurrentSession().createCriteria(Feed.class).
        addOrder(Order.desc("latestItemDate")).
        addOrder(Order.asc("name")).
        setCacheable(true).
        list();    
    }
	
	
	@SuppressWarnings("unchecked")
    final public List<Feed> getFeedsToRead() {
        return sessionFactory.getCurrentSession().createCriteria(Feed.class).
        add(Restrictions.ne("acceptancePolicy", "ignore")).
        addOrder(Order.asc("lastRead")).
        setCacheable(false).
        list();
    }
    
        
    @SuppressWarnings("unchecked")
    final public List<Resource> getAllCalendarFeeds() {
        return sessionFactory.getCurrentSession().createCriteria(CalendarFeed.class).       
        addOrder(Order.asc("name")). 
        setCacheable(true).
        list();    
    }
    
    
    @SuppressWarnings("unchecked")
    final public List<Resource> getAllWatchlists() {
        return sessionFactory.getCurrentSession().createCriteria(Watchlist.class).       
        addOrder(Order.asc("name")).
        setCacheable(true).
        list();
    }
    
    
    @SuppressWarnings("unchecked")
    // TODO add discovered timestamp and order by that.
    final public List<DiscoveredFeed> getAllDiscoveredFeeds() {
        return sessionFactory.getCurrentSession().createCriteria(DiscoveredFeed.class).
        setCacheable(true).
        addOrder(Order.desc("id")).
        list();                
    }
    
    
    @SuppressWarnings("unchecked")
	@Override
	public List<Newsitem> getNewsitemsForFeed(Feed feed) {
    	return sessionFactory.getCurrentSession().createCriteria(Newsitem.class).
    		add(Restrictions.eq("feed", feed)).
    		addOrder(Order.desc("date")).
    		list();
    }

    
    @SuppressWarnings("unchecked")
	@Override
	public List<PublishedResource> getNewsitemsForPublishers(Website publisher) {
    	return sessionFactory.getCurrentSession().createCriteria(Newsitem.class).
		add(Restrictions.eq("publisher", publisher)).
		list();
	}

    
	@SuppressWarnings("unchecked")  
    final public List<Resource> getOwnedBy(User owner, int maxItems) {    
        return sessionFactory.getCurrentSession().createCriteria(Resource.class).
                add(Restrictions.eq("owner", owner)).
                addOrder(Order.desc("date")).
                addOrder(Order.desc("id")).
                setMaxResults(maxItems).
                list();
    }
    
       
    @SuppressWarnings("unchecked")
    public List<Newsitem> getRecentUntaggedNewsitems() {
        return sessionFactory.getCurrentSession().createCriteria(Newsitem.class).
                add(Restrictions.isEmpty("tags")).
                add(Expression.eq("httpStatus", 200)).
                addOrder(Order.desc("date")).
                setMaxResults(12).
                setCacheable(true).list();        
    }
    
    
    @SuppressWarnings("unchecked")
    public List<Resource> getAllPublishersMatchingStem(String stem, boolean showBroken) {
        List<Resource> allPublishers = new ArrayList<Resource>();
        if (showBroken) {
            allPublishers = sessionFactory.getCurrentSession().createCriteria(Website.class).add(Restrictions.sqlRestriction(" page like \"%" + stem + "%\" ")).addOrder(Order.asc("name")).list();
        } else { 
            allPublishers = sessionFactory.getCurrentSession().createCriteria(Website.class).add(Restrictions.sqlRestriction(" page like \"%" + stem + "%\" ")).add(Expression.eq("httpStatus", 200)).addOrder(Order.asc("name")).list();            
        }               
        return allPublishers;
    }
    
        
    @SuppressWarnings("unchecked")
    // TODO migrate to a solr call
    public List<Resource> getNewsitemsMatchingStem(String stem) {
        return sessionFactory.getCurrentSession().createCriteria(Newsitem.class).add(Restrictions.sqlRestriction(" page like \"%" + stem + "%\" ")).addOrder(Order.asc("name")).list();        
    }
    
       
    @SuppressWarnings("unchecked")
    public List<Resource> getNotCheckedSince(Date oneMonthAgo, int maxItems) {     
        return sessionFactory.getCurrentSession().createCriteria(Resource.class).
        add(Restrictions.lt("lastScanned", oneMonthAgo)).addOrder(Order.asc("lastScanned")).
        setMaxResults(maxItems).list();       
    }
    
        
    @Override
	public List<Resource> getNotCheckedSince(Date launchedDate, Date lastScanned, int maxItems) {
    	   return sessionFactory.getCurrentSession().createCriteria(Resource.class).
    	   add(Restrictions.gt("liveTime", launchedDate)).
           add(Restrictions.lt("lastScanned", lastScanned)).
           addOrder(Order.asc("lastScanned")).
           setMaxResults(maxItems).list();       
	}


	@SuppressWarnings("unchecked")
    public List<CommentFeed> getCommentFeedsToCheck(int maxItems) {
        return sessionFactory.getCurrentSession().createCriteria(CommentFeed.class).
        addOrder(Order.desc("lastRead")).
        setCacheable(false).
        setMaxResults(maxItems).
        list();
    }
	
    @Override
	public int getOwnedByUserCount(User user) {
        return ((Long) sessionFactory.getCurrentSession().
        		iterate("select count(*) from ResourceImpl where owner = " + user.getId()).
        		next()).intValue();
	}


	public Resource loadResourceById(int resourceID) {
    	return (Resource) sessionFactory.getCurrentSession().get(ResourceImpl.class, resourceID);        
    }

    
    public Resource loadResourceByUrl(String url) {
        return (Resource) sessionFactory.getCurrentSession().createCriteria(Resource.class).add(Expression.eq("url", url)).setMaxResults(1).uniqueResult();        
    }
    
        
    @Override
	public Resource loadNewsitemByHeadlineAndPublisherWithinLastMonth(String name, Website publisher) {	// TODO last month clause
    	 return (Resource) sessionFactory.getCurrentSession().createCriteria(Newsitem.class).
    	 add(Expression.eq("name", name)).
    	 add(Expression.eq("publisher", publisher)).
    	 setMaxResults(1).uniqueResult();   
	}
    
        
	public Website getPublisherByUrlWords(String urlWords) {
		return (Website) sessionFactory.getCurrentSession().createCriteria(Website.class).add(Expression.eq("urlWords", urlWords)).setMaxResults(1).uniqueResult();    		
	}
	
	
	public Website getPublisherByName(String name) {
		return (Website) sessionFactory.getCurrentSession().createCriteria(Website.class).add(Expression.eq("name", name)).setMaxResults(1).uniqueResult();    		
	}
	
	
	public Feed loadFeedByUrlWords(String urlWords) {
		return (Feed) sessionFactory.getCurrentSession().createCriteria(Feed.class).add(Expression.eq("urlWords", urlWords)).setMaxResults(1).uniqueResult();
	}
        
    
	public Resource loadResourceByUniqueUrl(String url) {
        return (Resource) sessionFactory.getCurrentSession().createCriteria(Resource.class).add(Expression.eq("url", url)).uniqueResult();        
    }
    
        
    public CommentFeed loadCommentFeedByUrl(String url) {
        return (CommentFeed) sessionFactory.getCurrentSession().createCriteria(CommentFeed.class).add(Expression.eq("url", url)).setMaxResults(1).uniqueResult();  
    }
    
       
    public DiscoveredFeed loadDiscoveredFeedByUrl(String url) {
        return (DiscoveredFeed) sessionFactory.getCurrentSession().createCriteria(DiscoveredFeed.class).
        add(Expression.eq("url", url)).
        setMaxResults(1).
        setCacheable(true).
        uniqueResult();  
    }
    
    
	@Transactional
    public void saveResource(Resource resource) {
		if (resource.getType().equals("N")) {
			if (((Newsitem) resource).getImage() != null) {
				sessionFactory.getCurrentSession().saveOrUpdate(
						((Newsitem) resource).getImage());
			}
		}

		sessionFactory.getCurrentSession().saveOrUpdate(resource);
		if (resource.getType().equals("F")) {
			// TODO can this be done for just the publisher only?
			sessionFactory.evictCollection("nz.co.searchwellington.model.WebsiteImpl.feeds");
		}

		// TODO for related tags, can we be abit more subtle than this?
		// Clear related tags query.
		// sessionFactory.evictQueries();
	}

    
    
    public void saveDiscoveredFeed(DiscoveredFeed discoveredFeed) {
        sessionFactory.getCurrentSession().saveOrUpdate(discoveredFeed);
    }
    
    
    public void saveCommentFeed(CommentFeed commentFeed) {
        sessionFactory.getCurrentSession().saveOrUpdate(commentFeed);
    }


    @SuppressWarnings("unchecked")
    public List<Resource> getTaggedResources(Tag tag, int max_newsitems) {
        return sessionFactory.getCurrentSession().createCriteria(Resource.class).createCriteria("tags").add(Restrictions.eq("id", tag.getId())).list();
    }
    
    
    public void deleteResource(Resource resource) {
        sessionFactory.getCurrentSession().delete(resource);       
        // flush collection caches.  
        sessionFactory.evictCollection("nz.co.searchwellington.model.WebsiteImpl.feeds");
        sessionFactory.evictCollection("nz.co.searchwellington.model.WebsiteImpl.watchlist");
        sessionFactory.evictCollection("nz.co.searchwellington.model.DiscoveredFeed.references");
    }


    public List<Tag> getTagsMatchingKeywords(String keywords) {
        throw(new UnsupportedOperationException());
    }

    
    @SuppressWarnings("unchecked")
	public List<Resource> getResourcesWithTag(Tag tag) {
    	Criteria taggedResources = sessionFactory.getCurrentSession().createCriteria(Resource.class).
    		addOrder(Order.desc("date")).
    		addOrder(Order.desc("id")).
    		createAlias("tags", "rt").
    		add(Restrictions.eq("rt.id", tag.getId()));
    	
    	return taggedResources.list();
	}

}
