package nz.co.searchwellington.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedImpl;
import nz.co.searchwellington.model.FrontEndNewsitem;
import nz.co.searchwellington.model.FrontEndWebsite;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Watchlist;

import org.apache.solr.common.SolrDocument;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class SolrResourceHydratorTest {

	private static final int ID = 123;
	private static final String HEADLINE = "Test article";
	private static final Object URL = "http://localhost/somewhere";
	private static final int HTTP_STATUS = 200;
	private static final Date DATE = new DateTime().toDate();
	private static final String DESCRIPTION = "Description";
	private static final String PUBLISHER_NAME = "A Publisher";
	private static final String ADDRESS = "1 Someway";
	
	@Mock TagDAO tagDAO;
	
	private ResourceHydrator solrResourceHydrator;

	@Before
	public void setup() {
		solrResourceHydrator = new SolrResourceHydrator(tagDAO);
	}	
	
	@Test
	public void canHydrateNewsitem() throws Exception {	
		SolrDocument solrRow = buildSolrRecord("N");
		solrRow.setField("publisherName", PUBLISHER_NAME);
		solrRow.setField("geotagged", true);
		solrRow.setField("address", ADDRESS);
		solrRow.setField("position", "51,-0.1");
		
		FrontEndNewsitem hydratedNewsitem = (FrontEndNewsitem) solrResourceHydrator.hydrateResource(solrRow);

		assertBaseFields(hydratedNewsitem);
		assertEquals(PUBLISHER_NAME, hydratedNewsitem.getPublisherName());		
		assertNotNull(hydratedNewsitem.getGeocode());
		assertEquals(ADDRESS, hydratedNewsitem.getGeocode().getAddress());
	}
	
	@Test
	public void canHydrateWebsite() throws Exception {
		SolrDocument solrRow = buildSolrRecord("W");
		
		FrontEndWebsite hydratedWebsite = (FrontEndWebsite) solrResourceHydrator.hydrateResource(solrRow);
		
		assertBaseFields(hydratedWebsite);
	}
	
	@Test
	public void canHydrateWatchlistItem() throws Exception {
		SolrDocument solrRow = buildSolrRecord("L");
		solrRow.setField("publisherName", PUBLISHER_NAME);
		Watchlist hydratedWatchlist = (Watchlist) solrResourceHydrator.hydrateResource(solrRow);

		assertBaseFields(hydratedWatchlist);
		assertEquals(PUBLISHER_NAME, hydratedWatchlist.getPublisherName());
	}
	
	@Test
	public void testCanHydrateFeed() throws Exception {
		SolrDocument solrRow = buildSolrRecord("F");
		solrRow.setField("publisherName", PUBLISHER_NAME);
		
		Feed hydratedFeed = (FeedImpl) solrResourceHydrator.hydrateResource(solrRow);
		
		assertBaseFields(hydratedFeed);
		assertEquals(PUBLISHER_NAME, hydratedFeed.getPublisherName());
	}
	
	// TODO could be replaced with a call to the real solr row builder?
	private SolrDocument buildSolrRecord(String type) {
		SolrDocument solrRow = new SolrDocument();
		solrRow.setField("id", Integer.toString(ID));
		solrRow.setField("type", type);
		solrRow.setField("title", HEADLINE);
		solrRow.setField("url", URL);
		solrRow.setField("httpStatus", HTTP_STATUS);
		solrRow.setField("description", DESCRIPTION);
		solrRow.setField("date", DATE);
		return solrRow;
	}
	
	private void assertBaseFields(Resource hydratedResource) {
		assertEquals(123, hydratedResource.getId());
		assertEquals(HEADLINE, hydratedResource.getName());
		assertEquals(URL, hydratedResource.getUrl());
		assertEquals(HTTP_STATUS, hydratedResource.getHttpStatus());
		assertEquals(DATE, hydratedResource.getDate());		
		assertEquals(DESCRIPTION, hydratedResource.getDescription());
	}
	
}
