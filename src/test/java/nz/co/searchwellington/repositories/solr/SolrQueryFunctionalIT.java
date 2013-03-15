package nz.co.searchwellington.repositories.solr;

import java.net.MalformedURLException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Before;
import org.junit.Test;

public class SolrQueryFunctionalIT {
	
	private static final String SOLR_URL = "http://localhost:8080/apache-solr-3.2.0/twickenham";
	
	private CommonsHttpSolrServer solr;
	
	@Before
	public void setup() throws MalformedURLException {
		solr = new CommonsHttpSolrServer(SOLR_URL);
	}

	@Test
	public void testCanMakeLocationBasedQuery() throws Exception {				
		final SolrQuery newsitemsNearQuery = new SolrQueryBuilder().near(-41.33, 174.77, 5).showBroken(true).startIndex(0).maxItems(2).toQuery();
        QueryResponse response = timeSolrCall(newsitemsNearQuery, solr);
        response = timeSolrCall(newsitemsNearQuery, solr);
        
		SolrDocumentList results = response.getResults();
		for (SolrDocument solrDocument : results) {
			System.out.println(solrDocument.getFieldValue("title") + "," + solrDocument.getFieldValue("position"));
		}
	}
	
	@Test
	public void testTimeConnectionsThroughTheSolrQueryService() throws Exception {
		SolrQueryService solrQueryService = new SolrQueryService(solr);
		
		timeSolrServiceCall(new SolrQueryBuilder().geotagged().toQuery(), solrQueryService);
		final SolrQuery newsitemsNearQuery = new SolrQueryBuilder().near(-41.33, 174.77, 5).showBroken(true).startIndex(0).maxItems(2).toQuery();
        timeSolrServiceCall(newsitemsNearQuery, solrQueryService);
        timeSolrServiceCall(newsitemsNearQuery, solrQueryService);
        timeSolrServiceCall(newsitemsNearQuery, solrQueryService);
	}
	
	private void timeSolrServiceCall(SolrQuery query, SolrQueryService solrQueryService) {
		long startTime = System.currentTimeMillis();
		solrQueryService.querySolr(query);
		 long endTime = System.currentTimeMillis();		
			System.out.println(endTime - startTime);		
	}
	
	// TODO remove once we have proved we are reusing the solr connection in production
	private QueryResponse timeSolrCall(SolrQuery query, CommonsHttpSolrServer solr) throws SolrServerException {
		long startTime = System.currentTimeMillis();
		QueryResponse response = solr.query(query);
        long endTime = System.currentTimeMillis();		
		System.out.println(endTime - startTime);
		return response;
	}
	
}
