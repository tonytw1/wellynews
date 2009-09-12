package nz.co.searchwellington.repositories;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import nz.co.searchwellington.model.Resource;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.SolrInputDocument;

public class SolrQueryService {
	
	Logger log = Logger.getLogger(SolrQueryService.class);

	private String solrUrl;
	private SolrInputDocumentBuilder solrInputDocumentBuilder;
	
	public SolrQueryService(SolrInputDocumentBuilder solrInputDocumentBuilder) {
		this.solrInputDocumentBuilder = solrInputDocumentBuilder;
	}

	
	public QueryResponse querySolr(SolrQuery query) {
		SolrServer solr;
		try {
			solr = new CommonsHttpSolrServer(solrUrl);
			QueryResponse response = solr.query(query);
			return response;
		} catch (MalformedURLException e) {
			log.error(e);	
		} catch (SolrServerException e) {
			log.error(e);	
		}
		return null;
	}
	
	
	
	public List<Count> getFacetQueryResults(SolrQuery query, String facetFieldName) {
		QueryResponse response = querySolr(query);
		if (response != null) {
			FacetField facetField = response.getFacetField(facetFieldName);
			if (facetField != null && facetField.getValues() != null) {
				return facetField.getValues();				
			}
		}
		return null;
	}

	
	public void deleteResourceFromIndex(int id) {		
		try {
			SolrServer solr = new CommonsHttpSolrServer(solrUrl);				
			UpdateRequest updateRequest = new UpdateRequest();
			updateRequest.deleteById(Integer.toString(id));							
			updateRequest.process(solr);
			solr.commit();
			solr.optimize();
			
		} catch (MalformedURLException e) {
			log.error(e);	
		} catch (SolrServerException e) {
			log.error(e);	
		} catch (IOException e) {
			log.error(e);	
		}		
	}
	
	
	public void updateIndexForResource(Resource resource) {
		try {
			SolrServer solr = new CommonsHttpSolrServer(solrUrl);				
			UpdateRequest updateRequest = new UpdateRequest();					
			SolrInputDocument inputDocument = solrInputDocumentBuilder.buildResouceInputDocument(resource);
			updateRequest.add(inputDocument);					
			updateRequest.process(solr);
			solr.commit();
			solr.optimize();
			
		} catch (MalformedURLException e) {
			log.error(e);	
		} catch (SolrServerException e) {
			log.error(e);	
		} catch (IOException e) {
			log.error(e);
		}
	}


	public String getSolrUrl() {
		return solrUrl;
	}


	public void setSolrUrl(String solrUrl) {
		this.solrUrl = solrUrl;
	}
	
	
}
