package nz.co.searchwellington.repositories;

import static org.mockito.Mockito.when;
import nz.co.searchwellington.repositories.solr.SolrQueryBuilder;
import nz.co.searchwellington.repositories.solr.SolrQueryService;

import org.apache.solr.client.solrj.SolrServer;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SolrBackedResourceDAOTest {

	@Mock SolrServer solrServer;
	@Mock SolrQueryBuilderFactory solrQueryBuilderFactory;
	@Mock SolrQueryService solrQueryService;
	@Mock TagDAO tagDAO;
	@Mock SolrResourceHydrator resourceHydrator;
	private SolrQueryBuilder solrQueryBuilder = new SolrQueryBuilder();
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void canContructMonthQuery() throws Exception {
		when(solrQueryBuilderFactory.makeNewBuilder()).thenReturn(solrQueryBuilder);
		SolrBackedResourceDAO solrBackedResourceDAO = new SolrBackedResourceDAO(solrServer, solrQueryBuilderFactory, solrQueryService, tagDAO, resourceHydrator);
		final DateTime month = new DateTime(2009, 5, 1, 0, 0);

		solrBackedResourceDAO.getNewsitemsForMonth(month.toDate(), false);
		// TODO asserts
	}

}
