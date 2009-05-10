package nz.co.searchwellington.repositories;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;

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

public class SolrBackedResourceDAO extends LuceneBackedResourceDAO implements ResourceRepository {

    Logger log = Logger.getLogger(SolrBackedResourceDAO.class);
	final String solrUrl = "http://localhost:8080/apache-solr-1.3.0";
    
    
  
	public SolrBackedResourceDAO() {
	}
		
    public SolrBackedResourceDAO(SessionFactory sessionFactory, String indexPath, LuceneIndexUpdateService luceneIndexUpdateService) throws IOException {
        super(sessionFactory, indexPath, luceneIndexUpdateService);     
    }
    
    public List<Resource> getTaggedNewsitems(Tag tag, boolean showBroken, int startIndex, int maxItems) {    	
    	Set<Tag> tags = new HashSet<Tag>();
    	tags.add(tag);
    	return getTaggedNewsitems(tags, showBroken, startIndex, maxItems);
	}
    
       
	public List<Resource> getTaggedNewsitems(Set<Tag> name, boolean showBroken, int maxItems) {
		return getTaggedNewsitems(name, showBroken, 0, maxItems);
	}
	
	
	public int getTaggedNewitemsCount(Tag tag, boolean showBroken) {
		log.info("Getting newsitem count for tag: " + tag);		
		Set<Tag> tags = new HashSet<Tag>();
    	tags.add(tag);
		try {
			SolrServer solr = new CommonsHttpSolrServer(solrUrl);
			SolrQuery query = getTaggedContentSolrQuery(tags, showBroken, "N");
			QueryResponse response = solr.query(query);			
			Long count =  response.getResults().getNumFound();
			return count.intValue();			
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (SolrServerException e) {
			log.error(e);
		}
		return 0;
	}
	
	
	
	public List<Resource> getTaggedWebsites(Set<Tag> tags, boolean showBroken, int maxItems) {
		log.info("Getting websites for tags: " + tags );
		List<Resource> results = new ArrayList<Resource>();    	
    	try {
			SolrServer solr = new CommonsHttpSolrServer(solrUrl);
			SolrQuery query = getTaggedContentSolrQuery(tags, showBroken, "W");		
			query.setRows(maxItems);
			query.setSortField("name", ORDER.asc);
			
			QueryResponse response = solr.query(query);
			loadResourcesFromSolrResults(results, response);
			
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (SolrServerException e) {
			log.error(e);
		}    	
		return results;		
	}
	
	
	public List<Resource> getTaggedWebsites(Tag tag, boolean showBroken, int maxItems) {		
		Set<Tag> tags = new HashSet<Tag>();
    	tags.add(tag);
		return getTaggedWebsites(tags, showBroken, maxItems);
	}

	

	
	
	
	
	private List<Resource> getTaggedNewsitems(Set<Tag> tags, boolean showBroken, int startIndex, int maxItems) {
		log.info("Getting newsitems for tags: " + tags + " startIndex: " + startIndex + " maxItems: " + maxItems);
    	List<Resource> results = new ArrayList<Resource>();
    	
    	try {
			SolrServer solr = new CommonsHttpSolrServer(solrUrl);
			SolrQuery query = getTaggedContentSolrQuery(tags, showBroken, "N");
			query.setStart(startIndex);
			query.setRows(maxItems);
			query.setSortField("date", ORDER.desc);
			
			QueryResponse response = solr.query(query);
			loadResourcesFromSolrResults(results, response);
			
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (SolrServerException e) {
			log.error(e);
		}		
    	
		return results;
    }
    
   
    private SolrQuery getTaggedContentSolrQuery(Set<Tag> tags, boolean showBroken, String type) {
		StringBuilder sb= new StringBuilder();
		for (Tag tag : tags) {
			sb.append(" +tags:" + tag.getId());			
		}
		if (showBroken != true) {
			sb.append(" +httpStatus:200");
		}
		if (type != null) {
			sb.append(" +type:" + type);
		}		
		SolrQuery query = new SolrQuery(sb.toString().trim());
		return query;
	}
    
	private void loadResourcesFromSolrResults(List<Resource> results, QueryResponse response) {
		SolrDocumentList solrResults = response.getResults();
		for (SolrDocument result : solrResults) {
			final int resourceId = (Integer) result.getFieldValue("id");
			Resource resource = this.loadResourceById(resourceId);			
			results.add(resource);
		}
	}
    	    
	    
}
