package nz.co.searchwellington.repositories.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Test;

public class SolrQueryBuilderTest {

	@Test
	public void canBuildNearByQuery() throws Exception {
		SolrQueryBuilder solrQueryBuilder = new SolrQueryBuilder();
		SolrQuery newsitemsNearQuery = solrQueryBuilder.near(1, 2, 2).toQuery();
		System.out.println(newsitemsNearQuery.toString());
		// TODO asserts
	}
	
}
