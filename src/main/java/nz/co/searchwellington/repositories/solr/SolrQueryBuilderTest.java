package nz.co.searchwellington.repositories.solr;

import org.junit.Test;

public class SolrQueryBuilderTest {

	@Test
	public void canBuildNearByQuery() throws Exception {
		SolrQueryBuilder solrQueryBuilder = new SolrQueryBuilder();
		solrQueryBuilder.toNewsitemsNearQuery(1, 2, 2, false, 0, 5000);
	}
	
}
