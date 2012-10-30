package nz.co.searchwellington.repositories.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Test;

public class SolrQueryBuilderTest {

	@Test
	public void canBuildNearByQuery() throws Exception {
		SolrQueryBuilder solrQueryBuilder = new SolrQueryBuilder();
		SolrQuery newsitemsNearQuery = solrQueryBuilder.toNewsitemsNearQuery(1, 2, 2, false, 0, 5000);
		System.out.println(newsitemsNearQuery.toString());
	}
	
}
