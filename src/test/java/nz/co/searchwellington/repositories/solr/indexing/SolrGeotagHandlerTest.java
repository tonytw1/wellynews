package nz.co.searchwellington.repositories.solr.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class SolrGeotagHandlerTest {

	@Mock Resource resource;
	@Mock Set<Tag> indexTagsForResource;
	@Mock Tag placeTag;
	
	private SolrGeotagHandler solrGeotagHandler;
	private Geocode somewhere;
	private Geocode place;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		// = new SolrGeotagHandler();
		indexTagsForResource = new HashSet<Tag>();
		somewhere = new Geocode("Somewhere", 1, 2);
		place = new Geocode("A Place", 3, 4);
	}
	
	//@Test
	public void shouldMarkAsNotGetaggedIfNoneAvailable() throws Exception {
		SolrInputDocument inputDocument = new SolrInputDocument();
		//.processGeotags(resource, indexTagsForResource, inputDocument);		
		assertFalse((Boolean) inputDocument.getFieldValue("geotagged"));
	}

	//@Test
	public void shouldIndexResourceGeotagIfPresent() throws Exception {		
		Mockito.when(resource.getGeocode()).thenReturn(somewhere);
		
		SolrInputDocument inputDocument = new SolrInputDocument();
		//solrGeotagHandler.processGeotags(resource, indexTagsForResource, inputDocument);
		
		assertTrue((Boolean) inputDocument.getFieldValue("geotagged"));
		assertEquals("Somewhere", inputDocument.getFieldValue("address"));
		SolrInputField positionField = inputDocument.getField("position");
		assertEquals("1.0,2.0", positionField.getFirstValue());		
	}
	
	//@Test
	public void shouldFallBackToUsingTagGeotagsIfTheResourceIsNoTagged() throws Exception {
		SolrInputDocument inputDocument = new SolrInputDocument();
		Mockito.when(placeTag.getGeocode()).thenReturn(place);
		indexTagsForResource.add(placeTag);
		
		//.processGeotags(resource, indexTagsForResource, inputDocument);
		assertTrue((Boolean) inputDocument.getFieldValue("geotagged"));
		assertEquals("A Place", inputDocument.getFieldValue("address"));
		SolrInputField positionField = inputDocument.getField("position");
		assertEquals("3.0,4.0", positionField.getFirstValue());
	}
	
}
