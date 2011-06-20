package nz.co.searchwellington.repositories;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import nz.co.searchwellington.model.FrontEndNewsitem;
import nz.co.searchwellington.model.FrontEndWebsite;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.WebsiteImpl;

import org.apache.solr.common.SolrDocument;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class SolrResourceHydratorTest {

	private static final int ID = 123;
	private static final String HEADLINE = "Test article";
	private static final Date DATE = new DateTime().toDate();
	private static final String DESCRIPTION = "Description";
	private static final String PUBLISHER_NAME = "A Publisher";
	
	@Mock TagDAO tagDAO;

	private ResourceHydrator solrResourceHydrator;

	@Before
	public void setup() {
		solrResourceHydrator = new SolrResourceHydrator(tagDAO);
	}	
	
	@Test
	public void canHydrateNewsitem() throws Exception {	
		SolrDocument solrRow = buildSolrRecord("N");
		
		FrontEndNewsitem hydratedNewsitem = (FrontEndNewsitem) solrResourceHydrator.hydrateResource(solrRow);

		assertEquals(123, hydratedNewsitem.getId());
		assertEquals(HEADLINE, hydratedNewsitem.getName());
		assertEquals(DATE, hydratedNewsitem.getDate());		
		assertEquals(DESCRIPTION, hydratedNewsitem.getDescription());
		assertEquals(PUBLISHER_NAME, hydratedNewsitem.getPublisherName());
	}
	
	@Test
	public void canHydrateWebsite() throws Exception {
		SolrDocument solrRow = buildSolrRecord("W");
		
		FrontEndWebsite hydratedWebsite = (FrontEndWebsite) solrResourceHydrator.hydrateResource(solrRow);
		
		assertEquals(123, hydratedWebsite.getId());
		assertEquals(HEADLINE, hydratedWebsite.getName());
		assertEquals(DESCRIPTION, hydratedWebsite.getDescription());
		assertEquals(DATE, hydratedWebsite.getDate());
	}
	
	private SolrDocument buildSolrRecord(String type) {
		SolrDocument solrRow = new SolrDocument();
		solrRow.setField("id", Integer.toString(ID));
		solrRow.setField("type", type);
		solrRow.setField("title", HEADLINE);
		solrRow.setField("description", DESCRIPTION);
		solrRow.setField("date", DATE);
		solrRow.setField("publisherName", PUBLISHER_NAME);
		return solrRow;
	}
	
}
