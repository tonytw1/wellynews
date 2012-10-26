package nz.co.searchwellington.repositories;

import java.util.Date;

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
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void canContructMonthQuery() throws Exception {
		SolrBackedResourceDAO solrBackedResourceDAO = new SolrBackedResourceDAO(solrQueryBuilderFactory, solrQueryService, tagDAO, resourceHydrator);
		final Date month = new DateTime(2009, 5, 1, 0, 0).toDate();
		
		solrBackedResourceDAO.getNewsitemsForMonth(month, false);
		
		// TODO assert that the SolrQueryBuilder was called correctly.
	}
	
	@Test
	public void geoTaggedNewsitemsQueryShouldRespectShowBrokenFlag() {
		//SolrBackedResourceDAO solrBackedResourceDAO = new SolrBackedResourceDAO(solrQueryService, tagDAO, resourceHydrator);
		
		//solrBackedResourceDAO.getGeotaggedNewsitemsNear(1, 2, 1, false, 0, 20);
		//fail();
	}

}
