package nz.co.searchwellington.repositories;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
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
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
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
	
	
	public int getCommentedNewsitemsCount(boolean showBroken) {
		log.info("Getting commented newsitem count");
		try {
			SolrServer solr = new CommonsHttpSolrServer(solrUrl);
			SolrQuery query = getCommentedNewsitemsQuery(showBroken);
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

	
	public int getCommentedNewsitemsForTagCount(Tag tag, boolean showBroken) {
		log.info("Getting commented newsitem count for tag: " + tag);
		try {
			SolrServer solr = new CommonsHttpSolrServer(solrUrl);
			SolrQuery query = getCommentedNewsitemsForTagQuery(tag, showBroken);
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
		return getTaggedContent(tags, showBroken, "W", 0, maxItems, "name", ORDER.asc);
	}
	
	
	public List<Resource> getTaggedWebsites(Tag tag, boolean showBroken, int maxItems) {		
		Set<Tag> tags = new HashSet<Tag>();
		tags.add(tag);
		return getTaggedWebsites(tags, showBroken, maxItems);
	}
	
	
	public List<Resource> getTagWatchlist(Tag tag, boolean showBroken) {
		log.info("Getting watchlist for tag: " + tag);
		Set<Tag> tags = new HashSet<Tag>();
		tags.add(tag);
		return getTaggedContent(tags, showBroken, "L", 0, null, "name", ORDER.asc);
	}
	
	
	public List<Resource> getTaggedFeeds(Tag tag, boolean showBroken) {
		log.info("Getting feeds for tag: " + tag);
		Set<Tag> tags = new HashSet<Tag>();
		tags.add(tag);
		return getTaggedContent(tags, showBroken, "F", 0, null, "name", ORDER.asc);
	}
	
	
	public List<Resource> getCommentedNewsitems(int maxItems, boolean showBroken, boolean hasComments, int startIndex) {		
		List<Resource> results = new ArrayList<Resource>();
    	try {
			SolrServer solr = new CommonsHttpSolrServer(solrUrl);
			SolrQuery query = getCommentedNewsitemsQuery(showBroken);			
			query.setSortField("date", ORDER.desc);
			query.setRows(maxItems);
			query.setStart(startIndex);
			
			QueryResponse response = solr.query(query);
			loadResourcesFromSolrResults(results, response);
			
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (SolrServerException e) {
			log.error(e);
		}    	
		return results;
	}
	
	
	
	public List<Resource> getPublisherTagCombinerNewsitems(Website publisher, Tag tag, boolean showBroken) {
	
		List<Resource> results = new ArrayList<Resource>();
    	try {
			SolrServer solr = new CommonsHttpSolrServer(solrUrl);
			SolrQuery query = new SolrQueryBuilder().showBroken(showBroken).type("N").tag(tag).publisher(publisher).toQuery();			
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

	public List<Resource> getCommentedNewsitemsForTag(Tag tag, boolean showBroken, int maxItems, int startIndex) {
		List<Resource> results = new ArrayList<Resource>();
    	try {
			SolrServer solr = new CommonsHttpSolrServer(solrUrl);
			SolrQuery query = getCommentedNewsitemsForTagQuery(tag, showBroken);			
			query.setSortField("date", ORDER.desc);
			query.setStart(startIndex);
			query.setRows(maxItems);
			
			QueryResponse response = solr.query(query);
			loadResourcesFromSolrResults(results, response);
			
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (SolrServerException e) {
			log.error(e);
		}    	
		return results;
	}

	

	public List<Tag> getCommentedTags(boolean showBroken) {
		List<Integer> tagIds = new ArrayList<Integer>();
		try {
			SolrServer solr = new CommonsHttpSolrServer(solrUrl);
			SolrQuery query = getCommentedNewsitemsQuery(showBroken);			
			query.addFacetField("tags");
			query.setFacetMinCount(1);
			query.setFacetSort(true);
			
			QueryResponse response = solr.query(query);
			FacetField facetField = response.getFacetField("tags");
			if (facetField != null && facetField.getValues() != null) {
				log.info("Found facet field: " + facetField);
				List<Count> values = facetField.getValues();
				for (Count count : values) {
					final int tagId = Integer.parseInt(count.getName());
					tagIds.add(tagId);
				}				
			}
			
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (SolrServerException e) {
			log.error(e);
		}
		return loadTagsById(tagIds);
	}
	
	private List<Resource> getTaggedContent(Set<Tag> tags, boolean showBroken, String type, Integer startIndex, Integer maxItems, String orderField, ORDER order) {
		List<Resource> results = new ArrayList<Resource>();
    	try {
			SolrServer solr = new CommonsHttpSolrServer(solrUrl);
			SolrQuery query = getTaggedContentSolrQuery(tags, showBroken, type);		

			if (startIndex != null) {
				query.setStart(startIndex);
			}
			
			if (maxItems != null) {
				query.setRows(maxItems);
			}
			
			query.setSortField("name", ORDER.asc);
			if (orderField != null && order != null) {
				query.setSortField(orderField, order);
			}
			
			QueryResponse response = solr.query(query);
			loadResourcesFromSolrResults(results, response);
			
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (SolrServerException e) {
			log.error(e);
		}    	
		return results;		
	}
	
	
	
	private List<Resource> getTaggedNewsitems(Set<Tag> tags, boolean showBroken, int startIndex, int maxItems) {
		log.info("Getting newsitems for tags: " + tags + " startIndex: " + startIndex + " maxItems: " + maxItems);
    	return getTaggedContent(tags, showBroken, "N", startIndex, maxItems, "date", ORDER.desc);
    }
    
	
	private SolrQuery getCommentedNewsitemsQuery(boolean showBroken) {
		return new SolrQueryBuilder().showBroken(showBroken).type("N").commented(true).toQuery();			
	}
	
	
	private SolrQuery getCommentedNewsitemsForTagQuery(Tag tag, boolean showBroken) {
		return new SolrQueryBuilder().showBroken(showBroken).type("N").tag(tag).commented(true).toQuery();
	}
	
	private SolrQuery getTaggedContentSolrQuery(Set<Tag> tags, boolean showBroken, String type) {			
		return new SolrQueryBuilder().tags(tags).showBroken(showBroken).type(type).toQuery();		
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
