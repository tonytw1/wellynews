package nz.co.searchwellington.repositories;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import nz.co.searchwellington.repositories.solr.SolrQueryBuilder;
import nz.co.searchwellington.repositories.solr.SolrQueryService;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SolrBackedResourceDAOTest {

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
		SolrBackedResourceDAO solrBackedResourceDAO = new SolrBackedResourceDAO(solrQueryBuilderFactory, solrQueryService, tagDAO, resourceHydrator);
		final DateTime month = new DateTime(2009, 5, 1, 0, 0);

		solrBackedResourceDAO.getNewsitemsForMonth(month.toDate(), false);

		assertEquals("q=%2Btype%3AN+%2BhttpStatus%3A200+-embargoedUntil%3A%5BNOW+TO+*%5D+-held%3Atrue+%2Bdate%3A%5B2009-04-30T23%3A00%3A00.000Z+TO+2009-05-31T23%3A00%3A00.000Z%5D&sort=date+desc%2Cid+desc", solrQueryBuilder.toQuery().toString());
	}

}
