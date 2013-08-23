package nz.co.searchwellington.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchClientFactory;
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexUpdateService;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.GeoDistanceFilterBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.datehistogram.DateHistogramFacet;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.facet.terms.TermsFacet.ComparatorType;
import org.elasticsearch.search.facet.terms.TermsFacet.Entry;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ElasticSearchIT {

	private Client client;
	
	@Before
	public void setup() {
		client = new ElasticSearchClientFactory("elasticsearch", "ubuntu.local").getClient();
	}

	@Test
	public void canQuery() throws Exception {
		final SearchRequestBuilder requestBuilder = client.prepareSearch(
				ElasticSearchIndexUpdateService.INDEX).setTypes(
				ElasticSearchIndexUpdateService.TYPE);
		
		final SearchResponse response = requestBuilder.setQuery(
				QueryBuilders.termQuery("tagIds.id", "transport")).execute().actionGet();

		SearchHits hits = response.getHits();
		ObjectMapper objectMapper = new ObjectMapper();

		List<FrontendResource> resources = Lists.newArrayList();
		Iterator<SearchHit> iterator = hits.iterator();
		while (iterator.hasNext()) {
			SearchHit next = iterator.next();
			System.out.println(next.getSourceAsString());
			resources.add(objectMapper.readValue(next.getSourceAsString(), FrontendResource.class));
		}
		
		System.out.println(resources);
	}
	
	@Test
	public void canQueryForGeocodedNewsitems() throws Exception {
		final SearchRequestBuilder requestBuilder = client.prepareSearch(
				ElasticSearchIndexUpdateService.INDEX).setTypes(
				ElasticSearchIndexUpdateService.TYPE);
				
		final SearchResponse response = requestBuilder.setQuery(
				QueryBuilders.filtered(QueryBuilders.termQuery("type", "N"), FilterBuilders.existsFilter("place"))).
				execute().actionGet();
		
		SearchHits hits = response.getHits();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		final List<FrontendResource> resources = Lists.newArrayList();
		Iterator<SearchHit> iterator = hits.iterator();
		while (iterator.hasNext()) {
			SearchHit next = iterator.next();
			System.out.println(next.getSourceAsString());
			FrontendResource resource = objectMapper.readValue(next.getSourceAsString(), FrontendResource.class);
			assertNotNull(resource.getPlace());
			System.out.println(resource.getPlace());
			resources.add(resource);			
		}
		
		System.out.println(resources);
	}    
    
    @Test
	public void canQueryForGeocodedNewsitemsNearPoint() throws Exception {
		final SearchRequestBuilder requestBuilder = client.prepareSearch(
				ElasticSearchIndexUpdateService.INDEX).setTypes(
				ElasticSearchIndexUpdateService.TYPE);
		
		final GeoDistanceFilterBuilder geoFilter = FilterBuilders.geoDistanceFilter("location").distance("5km").point(-41.19, 174.95);
		
		final SearchResponse response = requestBuilder.setQuery(
				QueryBuilders.filtered(QueryBuilders.termQuery("type", "N"), geoFilter)).
				execute().actionGet();
		
		SearchHits hits = response.getHits();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		final List<FrontendResource> resources = Lists.newArrayList();
		Iterator<SearchHit> iterator = hits.iterator();
		while (iterator.hasNext()) {
			SearchHit next = iterator.next();
			FrontendResource resource = objectMapper.readValue(next.getSourceAsString(), FrontendResource.class);
			assertNotNull(resource.getPlace());
			System.out.println(resource.getPlace());
			resources.add(resource);			
		}
		
		System.out.println(resources);
	}
	
    
    
    
    
	@Test
	public void canFetchFacets() throws Exception {		
		final SearchRequestBuilder requestBuilder = client.prepareSearch(
				ElasticSearchIndexUpdateService.INDEX).setTypes(
				ElasticSearchIndexUpdateService.TYPE);
				
		final SearchResponse searchResponse = requestBuilder.setSize(0).
			addFacet(FacetBuilders.termsFacet("publisherName").field("publisherName").order(ComparatorType.TERM)).			
			execute().actionGet();
		System.out.println(searchResponse);
		
		 TermsFacet facet = (TermsFacet) searchResponse.getFacets().getFacets().get("publisherName");

		 final List<? extends Entry> entries = facet.getEntries();
         for (Entry entry : entries) {
                 System.out.println(entry.getTerm() + ": " + entry.getCount());
         }
         
         assertEquals(10, entries.size());
	}
	
	@Test
	public void canFetchDateFacets() throws Exception {
		final SearchRequestBuilder requestBuilder = client.prepareSearch(
				ElasticSearchIndexUpdateService.INDEX).setTypes(
				ElasticSearchIndexUpdateService.TYPE);
		
		final SearchResponse searchResponse = requestBuilder.setSize(0).
			addFacet(FacetBuilders.dateHistogramFacet("date").field("date").interval("month")).
			execute().actionGet();
		
		final DateHistogramFacet dateFacet = (DateHistogramFacet) searchResponse.getFacets().getFacets().get("date");
		Map<String, Long> facetMap = Maps.newTreeMap();
		for (org.elasticsearch.search.facet.datehistogram.DateHistogramFacet.Entry entry : dateFacet.getEntries()) {                    
    	   final String label = ISODateTimeFormat.basicDate().print(new DateTime(entry.getTime(), DateTimeZone.UTC));              
    	   facetMap.put(label, new Long(entry.getCount()));
		}
                 
		System.out.println(facetMap);           
	}
	
}