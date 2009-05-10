package nz.co.searchwellington.repositories;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hibernate.SessionFactory;

public class SolrBackedResourceDAO extends HibernateResourceDAO implements ResourceRepository {

    Logger log = Logger.getLogger(SolrBackedResourceDAO.class);
	final String solrUrl = "http://localhost:8080/apache-solr-1.3.0";
    
    private SolrQuery getTaggedContentSolrQuery(Tag tag, boolean showBroken, String type) {
		StringBuilder sb= new StringBuilder();
		sb.append("+tags:" + tag.getId());
		if (showBroken != true) {
			sb.append(" +httpStatus:200");
		}
		if (type != null) {
			sb.append(" +type:" + type);
		}		
		SolrQuery query = new SolrQuery(sb.toString());
		return query;
	}
  
	public SolrBackedResourceDAO() {
	}
		
    public SolrBackedResourceDAO(SessionFactory sessionFactory) {
        super(sessionFactory);     
    }

    
    public List<Resource> getTaggedNewitems(Tag tag, boolean showBroken, int startIndex, int maxItems) {
    	log.info("Getting newsitems for tag: " + tag);
    	List<Resource> results = new ArrayList<Resource>();
    	
    	try {
			SolrServer solr = new CommonsHttpSolrServer(solrUrl);
			SolrQuery query = getTaggedContentSolrQuery(tag, showBroken, "N");
			query.setStart(startIndex);
			query.setRows(maxItems);
			query.setSortField("date", ORDER.desc);
						
			QueryResponse response = solr.query(query);
			SolrDocumentList solrResults = response.getResults();
			for (SolrDocument result : solrResults) {
				final int resourceId = (Integer) result.getFieldValue("id");
				Resource resource = this.loadResourceById(resourceId);
				log.info(resource.getName());
				results.add(resource);
			}
			
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (SolrServerException e) {
			log.error(e);
		}		
    	
		// TODO Auto-generated method stub
		return results;
	}
    
    
    public List<Resource> getTaggedNewsitems(Set<Tag> name, boolean showBroken, int maxItems) {
    	// TODO Auto-generated method stub
    	return null;
    }
    
	public List<Resource> getAllValidGeocodedForTag(Tag tag, int maxNumber, boolean showBroken) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Resource> getCalendarFeedsForTag(Tag tag) {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getLastLiveTimeForTag(Tag tag) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Resource> getPublisherTagCombinerNewsitems(Website publisher,
			Tag tag, boolean showBroken) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Resource> getTaggedFeeds(Tag tag, boolean showBroken) {
		// TODO Auto-generated method stub
		return null;
	}

	

	public int getTaggedNewitemsCount(Tag tag, boolean showBroken) {
		// TODO Auto-generated method stub
		return 0;
	}


    
    
    
    
    
    
}
