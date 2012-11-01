package nz.co.searchwellington.repositories.solr;

import static org.junit.Assert.assertEquals;

import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Test;

public class SolrQueryBuilderTest {

	@Test
	public void canBuildNearByQuery() throws Exception {
		final SolrQuery newsitemsNearQuery = new SolrQueryBuilder().near(1, 2, 2).toQuery();

		assertEquals("q=*%3A*&fq=%7B%21geofilt%7D&sfield=position&pt=1.0%2C2.0&d=2.0&facet.mincount=1", newsitemsNearQuery.toString());
	}
	
	@Test
	public void canBuildLatestNewsitemsQuery() throws Exception {
		final SolrQuery latestNewsitemsQuery = new SolrQueryBuilder().type("N").toQuery();

		assertEquals("q=*%3A*&fq=%2Btype%3AN&facet.mincount=1", latestNewsitemsQuery.toString());
	}
	
}
