package nz.co.searchwellington.repositories.solr;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.SolrInputDocumentBuilder;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.SolrInputDocument;

public class SolrQueryService {
	
	private static Logger log = Logger.getLogger(SolrQueryService.class);

	private SolrInputDocumentBuilder solrInputDocumentBuilder;
	private SolrServer solr;
	
	public SolrQueryService(SolrInputDocumentBuilder solrInputDocumentBuilder, SolrServer solr) {
		this.solrInputDocumentBuilder = solrInputDocumentBuilder;
		this.solr = solr;
	}
	
	public QueryResponse querySolr(SolrQuery query) {
		try {		
			QueryResponse response = solr.query(query);
			return response;		
		} catch (SolrServerException e) {
			log.error(e);	
		}
		return null;
	}
		
	public Map<String, List<Count>> getFacetQueryResults(SolrQuery query) {
		Map<String, List<Count>> results = new HashMap<String, List<Count>>();
		QueryResponse response = querySolr(query);
		for (FacetField field : response.getFacetFields()) {
			if (field.getValues() != null) {
				results.put(field.getName(), field.getValues());
			}
		}
		return results;
	}
	
	public void deleteResourceFromIndex(int id) {		
		try {
			UpdateRequest updateRequest = new UpdateRequest();
			updateRequest.deleteById(Integer.toString(id));							
			updateRequest.process(solr);
			solr.commit();

		} catch (MalformedURLException e) {
			log.error(e);	
		} catch (SolrServerException e) {
			log.error(e);	
		} catch (IOException e) {
			log.error(e);	
		}		
	}
	
	public void updateIndexForResources(List<Resource> resources) {
		try {
			for (Resource resource : resources) {
				UpdateRequest updateRequest = getUpdateRequest(resource);
				updateRequest.process(solr);			
			}
			solr.commit();			
		} catch (IOException e) {
			log.error(e);
		} catch (SolrServerException e) {
			log.error(e);
		}
	}
	
	private UpdateRequest getUpdateRequest(Resource resource) {
		UpdateRequest updateRequest = new UpdateRequest();
		SolrInputDocument inputDocument = solrInputDocumentBuilder.buildResouceInputDocument(resource);
		updateRequest.add(inputDocument);
		return updateRequest;
	}
	
}
