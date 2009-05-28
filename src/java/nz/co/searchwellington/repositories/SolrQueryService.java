package nz.co.searchwellington.repositories;

import java.io.IOException;
import java.net.MalformedURLException;

import nz.co.searchwellington.model.Resource;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;

public class SolrQueryService {

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
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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


	public String getSolrUrl() {
		return solrUrl;
	}


	public void setSolrUrl(String solrUrl) {
		this.solrUrl = solrUrl;
	}
	
	
}
