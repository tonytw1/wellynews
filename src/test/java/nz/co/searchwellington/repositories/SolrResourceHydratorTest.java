package nz.co.searchwellington.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import nz.co.searchwellington.model.Twit;
import nz.co.searchwellington.model.frontend.FrontendFeed;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.model.frontend.FrontendWebsite;

import org.apache.solr.common.SolrDocument;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class SolrResourceHydratorTest {

	private static final String SECOND_COMMENT = "I for one welcome...";
	private static final String FIRST_COMMENT = "Someone is wrong in the Internet...";
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
		solrRow.setField("commented", false);
		
		FrontendNewsitem hydratedNewsitem = (FrontendNewsitem) solrResourceHydrator.hydrateResource(solrRow);

		assertBaseFields(hydratedNewsitem);
		assertEquals("N", hydratedNewsitem.getType());
		assertEquals(PUBLISHER_NAME, hydratedNewsitem.getPublisherName());		
		assertNotNull(hydratedNewsitem.getGeocode());
		assertEquals(ADDRESS, hydratedNewsitem.getGeocode().getAddress());
		assertEquals(51, hydratedNewsitem.getGeocode().getLatitude(), 0);
		assertEquals(-0.1, hydratedNewsitem.getGeocode().getLongitude(), 0);
	}
	
	@Test
	public void shouldHydrateNewsitemComments() throws Exception {
		SolrDocument solrRow = buildSolrRecord("N");
		solrRow.addField("commented", true);
		solrRow.addField("comment", FIRST_COMMENT);
		solrRow.addField("comment", SECOND_COMMENT);
		solrRow.addField("geotagged", false);
		
		FrontendNewsitem hydratedNewsitem = (FrontendNewsitem) solrResourceHydrator.hydrateResource(solrRow);
		
		assertNotNull(hydratedNewsitem.getComments());
		assertEquals(2, hydratedNewsitem.getComments().size());
		assertEquals(FIRST_COMMENT, hydratedNewsitem.getComments().get(0).getTitle());
		assertEquals(SECOND_COMMENT, hydratedNewsitem.getComments().get(1).getTitle());
	}
	
	@Test
	public void canHyrdateNewsitemTweets() throws Exception {
		SolrDocument solrRow = buildSolrRecord("N");
		solrRow.setField("commented", false);
		
		solrRow.setField("twitterCount", 2);
		solrRow.addField("geotagged", false);
		solrRow.addField("tweet_author", "tonytw1");
		solrRow.addField("tweet_text", "Blah");
		solrRow.addField("tweet_author", "someone");
		solrRow.addField("tweet_text", "Rant");
		
		FrontendNewsitem hydratedNewsitem = (FrontendNewsitem) solrResourceHydrator.hydrateResource(solrRow);
		assertEquals(2, hydratedNewsitem.getRetweets().size());
		Twit firstTweet = hydratedNewsitem.getRetweets().get(0);
		assertEquals("tonytw1", firstTweet.getAuthor());
		assertEquals("Blah", firstTweet.getText());
	}
	
	@Test
	public void canHydrateWebsite() throws Exception {
		SolrDocument solrRow = buildSolrRecord("W");
		solrRow.setField("urlWords", "url-words");
		
		FrontendWebsite hydratedWebsite = (FrontendWebsite) solrResourceHydrator.hydrateResource(solrRow);
		
		assertBaseFields(hydratedWebsite);
		assertEquals("W", hydratedWebsite.getType());
		assertEquals("url-words", hydratedWebsite.getUrlWords());	
	}
	
	@Test
	public void canHydrateWatchlistItem() throws Exception {
		SolrDocument solrRow = buildSolrRecord("L");
		solrRow.setField("publisherName", PUBLISHER_NAME);
		FrontendResource hydratedWatchlist = (FrontendResource) solrResourceHydrator.hydrateResource(solrRow);

		assertBaseFields(hydratedWatchlist);
		assertEquals("L", hydratedWatchlist.getType());
		//assertEquals(PUBLISHER_NAME, hydratedWatchlist.getPublisherName());
	}
	
	@Test
	public void testCanHydrateFeed() throws Exception {
		SolrDocument solrRow = buildSolrRecord("F");
		solrRow.setField("publisherName", PUBLISHER_NAME);
		solrRow.setField("urlWords", "my-teams-feed");
		
		FrontendFeed hydratedFeed = (FrontendFeed) solrResourceHydrator.hydrateResource(solrRow);
		
		assertBaseFields(hydratedFeed);
		assertEquals("F", hydratedFeed.getType());
		assertEquals(PUBLISHER_NAME, hydratedFeed.getPublisherName());
		assertEquals("my-teams-feed", hydratedFeed.getUrlWords());		
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
	
	private void assertBaseFields(FrontendResource hydratedResource) {
		assertEquals(123, hydratedResource.getId());
		assertEquals(HEADLINE, hydratedResource.getName());
		assertEquals(URL, hydratedResource.getUrl());
		assertEquals(HTTP_STATUS, hydratedResource.getHttpStatus());
		assertEquals(DATE, hydratedResource.getDate());		
		assertEquals(DESCRIPTION, hydratedResource.getDescription());
	}
	
}
