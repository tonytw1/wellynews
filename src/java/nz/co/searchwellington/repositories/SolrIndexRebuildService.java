package nz.co.searchwellington.repositories;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.PublishedResource;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;


public class SolrIndexRebuildService {

	Logger log = Logger.getLogger(SolrIndexRebuildService.class);

	private ResourceRepository resourceDAO;
		
	public SolrIndexRebuildService(ResourceRepository resourceDAO) {		
		this.resourceDAO = resourceDAO;		
	}


	public void buildIndex() {		
		Set<Integer> newsitemIdsToIndex = resourceDAO.getAllResourceIds();
		log.info("Number of resources to update in lucene index: " + newsitemIdsToIndex.size());
		try {
			SolrServer solr = new CommonsHttpSolrServer("http://localhost:8080/apache-solr-1.3.0");
			final String deleteAll = "*:*";
			UpdateResponse deleteAllQuery = solr.deleteByQuery(deleteAll);
			log.info(deleteAllQuery.toString());
			solr.commit(true, true);			
			solr.optimize();
	
			UpdateRequest updateRequest = new UpdateRequest();					
			for (Integer id : newsitemIdsToIndex) {
				Resource resource = resourceDAO.loadResourceById(id);
				log.info("Adding solr record: " + resource.getId() + " - " + resource.getName() + " - " + resource.getType());
			
				SolrInputDocument inputDocument = new SolrInputDocument();
				inputDocument.addField("id", resource.getId());
				inputDocument.addField("name", resource.getName());
				inputDocument.addField("type", resource.getType());
				inputDocument.addField("httpStatus", resource.getHttpStatus());
				inputDocument.addField("description", resource.getDescription());
				inputDocument.addField("date", resource.getDate());
				
				if (resource.getType().equals("N") && ((Newsitem) resource).getComments().size() > 0) {
					inputDocument.addField("commented", true);
				} else {
					inputDocument.addField("commented", false);
				}
				
				for(Tag tag: getIndexTagsForResource(resource)) {
					inputDocument.addField("tags", tag.getId());
				}
				
				Website publisher = getIndexPublisherForResource(resource);
				if (publisher != null) {
					inputDocument.addField("publisher", publisher.getId());
				}				
				updateRequest.add(inputDocument);
			}
			
			updateRequest.process(solr);
			solr.commit();
			solr.optimize();			
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}


	private Website getIndexPublisherForResource(Resource resource) {
		Website publisher = null;
		if (resource.getType().equals("N")){
			publisher = ((Newsitem) resource).getPublisher();
		}
		return publisher;
	}

	
	// TODO duplication with lucene index.
	private Set<Tag> getIndexTagsForResource(Resource resource) {	
		Set <Tag> indexTags = new HashSet<Tag>();
		indexTags.addAll(resource.getTags());
		
		final boolean shouldAppearOnPublisherAndParentTagPages = 
		    resource.getType().equals("L") || resource.getType().equals("N")
		    || resource.getType().equals("C") || resource.getType().equals("F");
				
		if (shouldAppearOnPublisherAndParentTagPages) {            
		    Set <Tag> existingTags = new HashSet<Tag>(indexTags);
		    for (Tag tag : existingTags) {
		        indexTags.addAll(tag.getAncestors());
		    }
		    
		    if (((PublishedResource) resource).getPublisher() != null) {              
		        for (Tag publisherTag : ((PublishedResource) resource).getPublisher().getTags()) {                
		            log.debug("Adding publisher tag " + publisherTag.getName() + " to record.");
		            indexTags.add(publisherTag);
		            indexTags.addAll(publisherTag.getAncestors());
		        }
		    }
		}
		
		return indexTags;
	}
	
}
