package nz.co.searchwellington.repositories;

import nz.co.searchwellington.repositories.solr.SolrQueryService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SolrBackedResourceDAOTest {

	@Mock SolrQueryService solrQueryService;
	@Mock TagDAO tagDAO;
	@Mock ResourceHydrator resourceHydrator;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void geoTaggedNewsitemsQueryShouldRespectShowBrokenFlag() {
		//SolrBackedResourceDAO solrBackedResourceDAO = new SolrBackedResourceDAO(solrQueryService, tagDAO, resourceHydrator);
		
		//solrBackedResourceDAO.getGeotaggedNewsitemsNear(1, 2, 1, false, 0, 20);
		//fail();
	}

}
