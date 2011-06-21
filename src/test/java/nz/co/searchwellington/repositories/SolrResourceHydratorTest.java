package nz.co.searchwellington.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedImpl;
import nz.co.searchwellington.model.FrontEndNewsitem;
import nz.co.searchwellington.model.FrontEndWebsite;
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
		solrRow.setField("geotagged", true);
		solrRow.setField("address", ADDRESS);
		solrRow.setField("position", "51,-0.1");
		
		FrontEndNewsitem hydratedNewsitem = (FrontEndNewsitem) solrResourceHydrator.hydrateResource(solrRow);

		assertEquals(123, hydratedNewsitem.getId());
		assertEquals(HEADLINE, hydratedNewsitem.getName());
		assertEquals(URL, hydratedNewsitem.getUrl());
		assertEquals(DATE, hydratedNewsitem.getDate());		
		assertEquals(DESCRIPTION, hydratedNewsitem.getDescription());
		assertEquals(PUBLISHER_NAME, hydratedNewsitem.getPublisherName());
		
		assertNotNull(hydratedNewsitem.getGeocode());
		assertEquals(ADDRESS, hydratedNewsitem.getGeocode().getAddress());
	}
	
	@Test
	public void canHydrateWebsite() throws Exception {
		SolrDocument solrRow = buildSolrRecord("W");
		
		FrontEndWebsite hydratedWebsite = (FrontEndWebsite) solrResourceHydrator.hydrateResource(solrRow);
		
		assertEquals(123, hydratedWebsite.getId());
		assertEquals(HEADLINE, hydratedWebsite.getName());
		assertEquals(URL, hydratedWebsite.getUrl());
		assertEquals(DESCRIPTION, hydratedWebsite.getDescription());
		assertEquals(DATE, hydratedWebsite.getDate());
	}
	
	@Test
	public void canHydrateWatchlistItem() throws Exception {
		SolrDocument solrRow = buildSolrRecord("L");
		
		Watchlist hydratedWatchlist = (Watchlist) solrResourceHydrator.hydrateResource(solrRow);

		assertEquals(123, hydratedWatchlist.getId());
		assertEquals(HEADLINE, hydratedWatchlist.getName());
		assertEquals(URL, hydratedWatchlist.getUrl());
		assertEquals(DESCRIPTION, hydratedWatchlist.getDescription());
		assertEquals(DATE, hydratedWatchlist.getDate());
		assertEquals(PUBLISHER_NAME, hydratedWatchlist.getPublisherName());

	}
	
	@Test
	public void testCanHydrateFeed() throws Exception {
		SolrDocument solrRow = buildSolrRecord("F");
		Feed hydratedFeed = (FeedImpl) solrResourceHydrator.hydrateResource(solrRow);
		
		assertEquals(123, hydratedFeed.getId());
		assertEquals(HEADLINE, hydratedFeed.getName());
		assertEquals(URL, hydratedFeed.getUrl());
		assertEquals(DESCRIPTION, hydratedFeed.getDescription());
		assertEquals(DATE, hydratedFeed.getDate());
		assertEquals(PUBLISHER_NAME, hydratedFeed.getPublisherName());

	}
	
	private SolrDocument buildSolrRecord(String type) {
		SolrDocument solrRow = new SolrDocument();
		solrRow.setField("id", Integer.toString(ID));
		solrRow.setField("type", type);
		solrRow.setField("title", HEADLINE);
		solrRow.setField("url", URL);
		solrRow.setField("description", DESCRIPTION);
		solrRow.setField("date", DATE);
		solrRow.setField("publisherName", PUBLISHER_NAME);
		return solrRow;
	}
	
}
