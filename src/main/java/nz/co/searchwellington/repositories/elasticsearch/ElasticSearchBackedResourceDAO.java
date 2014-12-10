package nz.co.searchwellington.repositories.elasticsearch;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.model.ArchiveLink;
import nz.co.searchwellington.model.PublisherContentCount;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.frontend.FrontendFeed;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;
import nz.co.searchwellington.model.frontend.FrontendResource;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceFilterBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.datehistogram.DateHistogramFacet;
import org.elasticsearch.search.facet.geodistance.GeoDistanceFacet;
import org.elasticsearch.search.facet.geodistance.GeoDistanceFacetBuilder;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.facet.terms.TermsFacet.ComparatorType;
import org.elasticsearch.search.facet.terms.TermsFacet.Entry;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.geo.model.LatLong;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ElasticSearchBackedResourceDAO {

	private static final String ID = "id";
	private static final String OWNER = "owner";
	private static final String HELD = "held";
	private static final String HTTP_STATUS = "httpStatus";
	private static final String PLACE = "place";
	private static final String LOCATION = "location";
	private static final int ALL = 1000;
	private static final String PUBLISHER_NAME = "publisherName";
	private static final String INDEX_TAGS = "tags.id";
	private static final String DATE = "date";
	private static final String NAME = "name";
	private static final String TYPE = "type";
	private static final String LATEST_ITEM_DATE = "latestItemDate";
	
	private final ElasticSearchClientFactory elasticSearchClientFactory;
	private final ObjectMapper objectMapper;
	private final LoggedInUserFilter loggedInUserFilter;	// TODO does this cause alot of ESBRDs to be spawned because LIUF is request scoped?
	
	@Autowired
	public ElasticSearchBackedResourceDAO(ElasticSearchClientFactory elasticSearchClientFactory, LoggedInUserFilter loggedInUserFilter) {
		this.elasticSearchClientFactory = elasticSearchClientFactory;
		this.loggedInUserFilter = loggedInUserFilter;
		this.objectMapper = new ObjectMapper();		
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	   }
	
	public List<FrontendResource> getLatestNewsitems(int maxItems, boolean shouldShowBroken) {
		final BoolQueryBuilder latestNewsitems = QueryBuilders.boolQuery().must(isNewsitem());
		addShouldShowBrokenClause(latestNewsitems, shouldShowBroken);
				
		final SearchRequestBuilder searchRequestBuilder = searchRequestBuilder().
			setQuery(latestNewsitems).
			setSize(maxItems);
		
		addDateDescendingOrder(searchRequestBuilder);
		
		final SearchResponse response = searchRequestBuilder.execute().actionGet();
		return deserializeFrontendResourceHits(response.getHits());
	}

	public List<FrontendResource> getTaggedNewsitems(Tag tag, boolean shouldShowBroken, int startIndex, int maxItems) {		
		final BoolQueryBuilder tagNewsitemsQuery = tagNewsitemsQuery(tag);
		addShouldShowBrokenClause(tagNewsitemsQuery, shouldShowBroken);
		
		final SearchRequestBuilder searchRequestBuilder = searchRequestBuilder().
			setQuery(tagNewsitemsQuery).
			setFrom(startIndex).
			setSize(maxItems);
		
		addDateDescendingOrder(searchRequestBuilder);
		
		final SearchResponse response = searchRequestBuilder.execute().actionGet();
		return deserializeFrontendResourceHits(response.getHits());
	}
	
	public List<FrontendResource> getPublisherNewsitems(Website publisher, int maxItems, boolean shouldShowBroken, int startIndex) {
		final SearchResponse response = publisherNewsitemsRequest(publisher, maxItems, shouldShowBroken, startIndex).execute().actionGet();
		return deserializeFrontendResourceHits(response.getHits());
	}

	public long getPublisherNewsitemsCount(Website publisher, boolean shouldShowBroken) {
		final SearchResponse response = publisherNewsitemsRequest(publisher, 0, shouldShowBroken, 0).execute().actionGet();
		return response.getHits().getTotalHits();
	}
	
	public long getTaggedNewitemsCount(Tag tag, boolean shouldShowBroken) {
		final SearchRequestBuilder searchRequestBuilder = searchRequestBuilder().
			setQuery(tagNewsitemsQuery(tag));
		
		final SearchResponse response = searchRequestBuilder.execute().actionGet();
		return response.getHits().getTotalHits();
	}
	
	public List<FrontendResource> getLatestWebsites(int maxItems, boolean shouldShowBroken) {
		final TermQueryBuilder isWebsite = QueryBuilders.termQuery(TYPE, "W");
		final BoolQueryBuilder websites = QueryBuilders.boolQuery().must(isWebsite);

		addShouldShowBrokenClause(websites, shouldShowBroken);
		
		final SearchRequestBuilder justinWebsites = searchRequestBuilder().setQuery(websites).setSize(maxItems);
		addDateDescendingOrder(justinWebsites);
		
		final SearchResponse response = justinWebsites.execute().actionGet();
		return deserializeFrontendResourceHits(response.getHits());
	}
	
	public List<FrontendResource> getTaggedWebsites(Set<Tag> tags, boolean shouldShowBroken, int maxItems) {
		final TermQueryBuilder isWebsite = QueryBuilders.termQuery(TYPE, "W");		
		final BoolQueryBuilder taggedWebsites = QueryBuilders.boolQuery().must(isWebsite);
		for (Tag tag : tags) {
			taggedWebsites.must(hasTag(tag));
		}
		
		addShouldShowBrokenClause(taggedWebsites, shouldShowBroken);
		
		final SearchRequestBuilder searchRequestBuilder = searchRequestBuilder().
			setQuery(taggedWebsites).
			setSize(maxItems);
	
		addNameOrder(searchRequestBuilder);
	
		final SearchResponse response = searchRequestBuilder.execute().actionGet();
		return deserializeFrontendResourceHits(response.getHits());
	}
	
	public List<FrontendResource> getTagWatchlist(Tag tag, boolean shouldShowBroken) {
		final BoolQueryBuilder taggedWatchlists = QueryBuilders.boolQuery().must(isWatchlist()).must(hasTag(tag));		
		final SearchRequestBuilder searchRequestBuilder = searchRequestBuilder().
			setQuery(taggedWatchlists).
			setSize(ALL);

		addShouldShowBrokenClause(taggedWatchlists, shouldShowBroken);
		addNameOrder(searchRequestBuilder);
	
		final SearchResponse response = searchRequestBuilder.execute().actionGet();
		return deserializeFrontendResourceHits(response.getHits());
	}
	
	public List<FrontendResource> getTaggedFeeds(Tag tag, boolean shouldShowBroken) {
		final BoolQueryBuilder taggedFeeds = QueryBuilders.boolQuery().must(isFeed()).must(hasTag(tag));
		final SearchRequestBuilder searchRequestBuilder = searchRequestBuilder().setQuery(taggedFeeds).setSize(ALL);
		
		addShouldShowBrokenClause(taggedFeeds, shouldShowBroken);
		addNameOrder(searchRequestBuilder);

		final SearchResponse response = searchRequestBuilder.execute().actionGet();
		return deserializeFrontendResourceHits(response.getHits());
	}
	
	public List<FrontendResource> getPublisherWatchlist(Website publisher, boolean shouldShowBroken) {
		final BoolQueryBuilder publisherWatchlist = QueryBuilders.boolQuery().must(isWatchlist()).must(hasPublisher(publisher));
		
		addShouldShowBrokenClause(publisherWatchlist, shouldShowBroken);
		
		final SearchRequestBuilder searchRequestBuilder = searchRequestBuilder().
			setQuery(publisherWatchlist).
			setSize(ALL);
		
		addNameOrder(searchRequestBuilder);
	
		final SearchResponse response = searchRequestBuilder.execute().actionGet();
		return deserializeFrontendResourceHits(response.getHits());
	}
	
	public List<FrontendResource> getPublisherFeeds(Website publisher, boolean shouldShowBroken) {
		final BoolQueryBuilder publisherFeeds = QueryBuilders.boolQuery().must(isFeed()).must(hasPublisher(publisher));		
		final SearchRequestBuilder searchRequestBuilder = searchRequestBuilder().
			setQuery(publisherFeeds).
			setSize(ALL);
		
		addShouldShowBrokenClause(publisherFeeds, shouldShowBroken);
		addNameOrder(searchRequestBuilder);
	
		final SearchResponse response = searchRequestBuilder.execute().actionGet();
		return deserializeFrontendResourceHits(response.getHits());
	}
	
	public List<FrontendResource> getAllWatchlists(boolean shouldShowBroken) {	
		final SearchResponse response = searchRequestBuilder().setQuery(isWatchlist()).setSize(ALL).execute().actionGet();
		return deserializeFrontendResourceHits(response.getHits());
	}
	
	public List<FrontendResource> getAllFeeds(boolean shouldShowBroken, boolean latestFirst) {		
		final BoolQueryBuilder feeds = QueryBuilders.boolQuery().must(isFeed());
		addShouldShowBrokenClause(feeds, shouldShowBroken);
			
		final SearchRequestBuilder searchRequestBuilder = searchRequestBuilder().setQuery(feeds).setSize(ALL);
		
		if (latestFirst) {
			addLatestFeedItemOrder(searchRequestBuilder);
		} else {
			addNameOrder(searchRequestBuilder);
		}
		
		final SearchResponse response = searchRequestBuilder.execute().actionGet();
		return deserializeFrontendResourceHits(response.getHits());
	}

	public List<FrontendResource> getGeotagged(int startIndex, int maxItems, boolean shouldShowBroken) {		
		final SearchRequestBuilder searchRequestBuilder = searchRequestBuilder().
			setQuery(geotaggedNewsitems(shouldShowBroken)).
			setFrom(startIndex).
			setSize(maxItems);
		
		addDateDescendingOrder(searchRequestBuilder);
		
		final SearchResponse response = searchRequestBuilder.execute().actionGet();
		return deserializeFrontendResourceHits(response.getHits());
	}

	public long getGeotaggedCount(boolean shouldShowBroken) {
		final SearchRequestBuilder searchRequestBuilder = searchRequestBuilder().
		setQuery(geotaggedNewsitems(shouldShowBroken)).
		setSize(0);
		
		final SearchResponse response = searchRequestBuilder.execute().actionGet();
		return response.getHits().getTotalHits();
	}
	
	public List<FrontendResource> getGeotaggedNewsitemsNear(LatLong latLong, double radius, boolean shouldShowBroken, int startIndex, int maxItems) {
		final SearchRequestBuilder searchRequestBuilder = searchRequestBuilder().
			setQuery(geotaggedNearQuery(latLong, radius, shouldShowBroken)).
			setFrom(startIndex).
			setSize(maxItems);
		
		addDateDescendingOrder(searchRequestBuilder);
		
		final SearchResponse response = searchRequestBuilder.execute().actionGet();
		return deserializeFrontendResourceHits(response.getHits());
	}
	
	public Map<String, Integer> getPublisherFacetsNear(LatLong latLong, double radius, boolean shouldShowBroken) {
		final SearchResponse searchResponse = searchRequestBuilder().setQuery(geotaggedNearQuery(latLong, radius, shouldShowBroken)).
				addFacet(FacetBuilders.termsFacet(PUBLISHER_NAME).field(PUBLISHER_NAME).order(ComparatorType.COUNT).size(10)).
				execute().actionGet();
	
		final Map<String, Integer> facets = Maps.newLinkedHashMap();
		final TermsFacet facet = (TermsFacet) searchResponse.getFacets().getFacets().get(PUBLISHER_NAME);
	    for (Entry entry : (List<? extends Entry>) facet.getEntries()) {
			facets.put(entry.getTerm().string(), entry.getCount());
	    }
		return facets;		
	}
	
	public Map<Double, Long> getNewsitemsNearDistanceFacet(LatLong latLong, boolean shouldShowBroken) {
		final GeoDistanceFacetBuilder geoDistanceFacet = FacetBuilders.geoDistanceFacet("distance").field("location").lat(latLong.getLatitude()).lon(latLong.getLongitude());
		geoDistanceFacet.unit(DistanceUnit.KILOMETERS);
		for (int i = 1; i <= 9; i++) {
			geoDistanceFacet.addRange(0, i);			
		}
		for (int i = 10; i <= 50; i = i + 10) {
			geoDistanceFacet.addRange(0, i);			
		}
		
		final SearchRequestBuilder searchRequestBuilder = searchRequestBuilder().
			setQuery(geotaggedNearQuery(latLong, 1000, shouldShowBroken)).
			setSize(0).addFacet(geoDistanceFacet);
		
		final SearchResponse response = searchRequestBuilder.execute().actionGet();
				
		final Map<Double, Long> distanceFacets = Maps.newLinkedHashMap();
		final GeoDistanceFacet facets = (GeoDistanceFacet) response.getFacets().facetsAsMap().get("distance");
		for (GeoDistanceFacet.Entry entry : facets) {
			distanceFacets.put(entry.getTo(), entry.getCount());
		}
		return distanceFacets;
	}
	
	public long getGeotaggedNewsitemsNearCount(LatLong latLong, double radius, boolean shouldShowBroken) {
		final SearchRequestBuilder searchRequestBuilder = searchRequestBuilder().
			setQuery(geotaggedNearQuery(latLong, radius, shouldShowBroken)).
			setSize(0);
		
		final SearchResponse response = searchRequestBuilder.execute().actionGet();
		return response.getHits().getTotalHits();
	}

	public List<PublisherContentCount> getAllPublishers(boolean shouldShowBroken) {		
		final SearchResponse searchResponse = searchRequestBuilder().setSize(0).
			addFacet(FacetBuilders.termsFacet(PUBLISHER_NAME).field(PUBLISHER_NAME).order(ComparatorType.TERM).size(Integer.MAX_VALUE)).
			execute().actionGet();
		
		final List<PublisherContentCount> publishers = Lists.newArrayList();
		final TermsFacet facet = (TermsFacet) searchResponse.getFacets().getFacets().get(PUBLISHER_NAME);
        List<? extends Entry> entries = facet.getEntries();
        for (Entry entry : entries) {
        	publishers.add(new PublisherContentCount(entry.getTerm().string(), entry.getCount()));
        }		
		return publishers;
	}
	
	public Map<String, Integer> getTagFacetsForTag(Tag tag) {		
		return tagNewsitemsFacet(tag, INDEX_TAGS);		
	}
	
	public Map<String, Integer> getPublisherFacetsForTag(Tag tag) {
		return tagNewsitemsFacet(tag, PUBLISHER_NAME);
	}
	
	public int getCommentedNewsitemsForTagCount(Tag tag, boolean shouldShowBroken) {
		return 0;	// TODO implement
	}

	public List<FrontendResource> getRecentCommentedNewsitemsForTag(Tag tag, boolean shouldShowBroken, int maxItems) {
		return Lists.newArrayList();	// TODO implement
	}
	
	public List<FrontendResource> getTaggedGeotaggedNewsitems(Tag tag, int maxItems, boolean shouldShowBroken) {
		return Lists.newArrayList();	// TODO implement
	}
	
	public List<Tag> getGeotaggedTags(boolean shouldShowBroken) {
		return Lists.newArrayList();	// TODO implement
	}

	public List<FrontendResource> getCommentedNewsitems(int maxItems, boolean shouldShowBroken, boolean b, int startIndex) {
		return Lists.newArrayList();	// TODO implement
	}
	
	public int getCommentedNewsitemsCount(boolean shouldShowBroken) {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<Tag> getCommentedTags(boolean shouldShowBroken) {
		// TODO Auto-generated method stub
		return Lists.newArrayList();	// TODO implement
	}
	
	public List<FrontendResource> getTwitteredNewsitems(int startIndex, int maxItems, boolean shouldShowBroken) {
		final SearchRequestBuilder searchRequestBuilder = searchRequestBuilder().
			setQuery(twitternMentionedNewsitemsQuery(shouldShowBroken)).
			setSize(maxItems);
		
		addDateDescendingOrder(searchRequestBuilder);
		
		final SearchResponse response = searchRequestBuilder.execute().actionGet();
		return deserializeFrontendResourceHits(response.getHits());		
	}

	public long getTwitteredNewsitemsCount(boolean shouldShowBroken) {
		final SearchRequestBuilder searchRequestBuilder = searchRequestBuilder().
			setQuery(twitternMentionedNewsitemsQuery(shouldShowBroken)).
			setSize(0);
	
		final SearchResponse response = searchRequestBuilder.execute().actionGet();
		return response.getHits().getTotalHits();
	}
	
	public List<FrontendResource> getRecentTwitteredNewsitemsForTag(int maxItems, boolean shouldShowBroken, Tag tag) {
		// TODO Auto-generated method stub
		return Lists.newArrayList();	// TODO implement
	}
	
	public List<ArchiveLink> getArchiveMonths(boolean shouldShowBroken) {		
		final BoolQueryBuilder latestNewsitems = QueryBuilders.boolQuery().must(isNewsitem());
		addShouldShowBrokenClause(latestNewsitems, shouldShowBroken);
		
		final SearchResponse searchResponse = searchRequestBuilder().setQuery(latestNewsitems).
			setSize(0).
			addFacet(FacetBuilders.dateHistogramFacet(DATE).field(DATE).interval("month")).
			execute().actionGet();
		
		final DateHistogramFacet dateFacet = (DateHistogramFacet) searchResponse.getFacets().getFacets().get(DATE);
				
		final List<ArchiveLink> archiveMonths = Lists.newArrayList();
		for (org.elasticsearch.search.facet.datehistogram.DateHistogramFacet.Entry entry : dateFacet.getEntries()) {
			final DateTime monthDate = new DateTime(entry.getTime(), DateTimeZone.UTC);		
			archiveMonths.add(new ArchiveLink(monthDate.toDate(), entry.getCount()));
		}		
		Collections.reverse(archiveMonths);
		return archiveMonths;
	}
	
	public Map<String, Integer> getArchiveStatistics(boolean shouldShowBroken) {
		final Map<String, Integer> contentCounts = Maps.newHashMap();
		
		final SearchResponse searchResponse = searchRequestBuilder().
			addFacet(FacetBuilders.termsFacet(TYPE).field(TYPE)).
			execute().actionGet();
	
		final TermsFacet facet = (TermsFacet) searchResponse.getFacets().getFacets().get(TYPE);
		for (Entry entry : (List<? extends Entry>) facet.getEntries()) {
			contentCounts.put(entry.getTerm().string(), entry.getCount());
		}		
		return contentCounts;		
	}
	
	public List<FrontendResource> getCommentedNewsitemsForTag(Tag tag, boolean shouldShowBroken, int maxNewsitems, int startIndex) {
		// TODO Auto-generated method stub
		return Lists.newArrayList();	// TODO implement
	}

	public List<FrontendResource> getNewsitemsForMonth(Date month, boolean shouldShowBroken) {
		DateTime monthDateTime = new DateTime(month);
		DateTime startOfMonth = monthDateTime.toDateMidnight().withDayOfMonth(1).toDateTime();
		DateTime endOfMonth = startOfMonth.plusMonths(1);
		
		final BoolQueryBuilder latestNewsitems = QueryBuilders.boolQuery().must(isNewsitem()).
			must(QueryBuilders.rangeQuery(DATE).from(Long.toString(startOfMonth.toDate().getTime()))).
			must(QueryBuilders.rangeQuery(DATE).to(Long.toString(endOfMonth.toDate().getTime())));
		addShouldShowBrokenClause(latestNewsitems, shouldShowBroken);
				
		final SearchRequestBuilder searchRequestBuilder = searchRequestBuilder().
			setQuery(latestNewsitems).
			setSize(ALL);
		
		addDateDescendingOrder(searchRequestBuilder);
		
		final SearchResponse response = searchRequestBuilder.execute().actionGet();
		return deserializeFrontendResourceHits(response.getHits());
	}
	
	public List<FrontendResource> getTaggedNewsitems(List<Tag> tags, boolean shouldShowBroken, int startIndex, int maxNewsitems) {
		final SearchRequestBuilder searchRequestBuilder = tagCombinerQuery(tags, shouldShowBroken, maxNewsitems);
		
		addNameOrder(searchRequestBuilder);
	
		final SearchResponse response = searchRequestBuilder.execute().actionGet();
		return deserializeFrontendResourceHits(response.getHits());
	}

	public long getTaggedNewsitemsCount(List<Tag> tags, boolean shouldShowBroken) {
		final SearchRequestBuilder searchRequestBuilder = tagCombinerQuery(tags, shouldShowBroken, 0);			
		final SearchResponse response = searchRequestBuilder.execute().actionGet();
		return response.getHits().getTotalHits();
	}
	
	public List<FrontendResource> getPublisherTagCombinerNewsitems(Website publisher, Tag tag, boolean shouldShowBroken, int maxNewsitems) {
		final BoolQueryBuilder publishertaggedNewsitems = QueryBuilders.boolQuery().must(QueryBuilders.termQuery(TYPE, "N"));
		publishertaggedNewsitems.must(hasTag(tag));
		publishertaggedNewsitems.must(hasPublisher(publisher));
		
		addShouldShowBrokenClause(publishertaggedNewsitems, shouldShowBroken);
		
		final SearchRequestBuilder searchRequestBuilder = searchRequestBuilder().
			setQuery(publishertaggedNewsitems).
			setSize(maxNewsitems);
		
		final SearchResponse response = searchRequestBuilder.execute().actionGet();
		return deserializeFrontendResourceHits(response.getHits());
	}

	public List<FrontendResource> getHandTaggingsForUser(User user, boolean shouldShowBroken) {
		// TODO Auto-generated method stub
		return Lists.newArrayList();	// TODO implement
	}

	public FrontendResource getNewspage(String urlWords, boolean shouldShowBroken) {		
		final BoolQueryBuilder urlWordsQuery = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("urlWords", urlWords));		
		addShouldShowBrokenClause(urlWordsQuery, shouldShowBroken);
		
		final SearchRequestBuilder searchRequestBuilder = searchRequestBuilder().
			setQuery(urlWordsQuery).
			setSize(1);
		
		final SearchResponse response = searchRequestBuilder.execute().actionGet();
		if (response.getHits().getTotalHits() > 0) {
			return deserializeFrontendResourceHits(response.getHits()).get(0);
		}
		return null;
	}
	
	private FilteredQueryBuilder twitternMentionedNewsitemsQuery(
			boolean shouldShowBroken) {
		final BoolQueryBuilder latestNewsitems = QueryBuilders.boolQuery().must(isNewsitem());
		addShouldShowBrokenClause(latestNewsitems, shouldShowBroken);
		final FilteredQueryBuilder twitterMentionedNewsitemsQuery = QueryBuilders.filtered(latestNewsitems, FilterBuilders.existsFilter("twitterMentions"));
		return twitterMentionedNewsitemsQuery;
	}

	private FilteredQueryBuilder geotaggedNearQuery(LatLong latLong, double radius, boolean shouldShowBroken) {
		final BoolQueryBuilder geotaggedNear = QueryBuilders.boolQuery().must(isNewsitem());
		addShouldShowBrokenClause(geotaggedNear, shouldShowBroken);
		
		final GeoDistanceFilterBuilder nearFilter = FilterBuilders.geoDistanceFilter(LOCATION).
			distance(Double.toString(radius) + "km").
			point(latLong.getLatitude(), latLong.getLongitude());
		
		return QueryBuilders.filtered(geotaggedNear, nearFilter);
	}
	
	private Map<String, Integer> tagNewsitemsFacet(Tag tag, String facetField) {
		final SearchResponse searchResponse = searchRequestBuilder().setQuery(tagNewsitemsQuery(tag)).
			addFacet(FacetBuilders.termsFacet(facetField).field(facetField).order(ComparatorType.COUNT).size(10)).
			execute().actionGet();
		
		final Map<String, Integer> facets = Maps.newLinkedHashMap();
		final TermsFacet facet = (TermsFacet) searchResponse.getFacets().getFacets().get(facetField);
        for (Entry entry : (List<? extends Entry>) facet.getEntries()) {       
			facets.put(entry.getTerm().string(), entry.getCount());
        }
		return facets;
	}
	
	private BoolQueryBuilder tagNewsitemsQuery(Tag tag) {
		return QueryBuilders.boolQuery().must(hasTag(tag)).must(isNewsitem());
	}
	
	private SearchRequestBuilder publisherNewsitemsRequest(Website publisher, int maxItems, boolean shouldShowBroken, int startIndex) {
		final BoolQueryBuilder publisherNewsitemsQuery = QueryBuilders.boolQuery();
		publisherNewsitemsQuery.must(isNewsitem()).must(hasPublisher(publisher));
		addShouldShowBrokenClause(publisherNewsitemsQuery, shouldShowBroken);
		
		final SearchRequestBuilder searchRequestBuilder = searchRequestBuilder().
			setQuery(publisherNewsitemsQuery).
			setFrom(startIndex).
			setSize(maxItems);
	
		addDateDescendingOrder(searchRequestBuilder);
		return searchRequestBuilder;
	}
	
	private SearchRequestBuilder tagCombinerQuery(List<Tag> tags, boolean shouldShowBroken, int maxNewsitems) {
		final BoolQueryBuilder taggedNewsitems = QueryBuilders.boolQuery().must(QueryBuilders.termQuery(TYPE, "N"));
		for (Tag tag : tags) {
			taggedNewsitems.must(hasTag(tag));
		}
		
		addShouldShowBrokenClause(taggedNewsitems, shouldShowBroken);
		
		final SearchRequestBuilder searchRequestBuilder = searchRequestBuilder().
			setQuery(taggedNewsitems).
			setSize(maxNewsitems);
		return searchRequestBuilder;
	}
	
	private FilteredQueryBuilder geotaggedNewsitems(boolean shouldShowBroken) {
		final BoolQueryBuilder geotaggedNewsitems = QueryBuilders.boolQuery().must(isNewsitem());
		addShouldShowBrokenClause(geotaggedNewsitems, shouldShowBroken);
				
		return QueryBuilders.filtered(geotaggedNewsitems, FilterBuilders.existsFilter(PLACE));
	}

	private TermQueryBuilder hasPublisher(Website publisher) {
		return QueryBuilders.termQuery(PUBLISHER_NAME, publisher.getName());
	}

	private TermQueryBuilder hasTag(Tag tag) {
		return QueryBuilders.termQuery(INDEX_TAGS, tag.getName());
	}

	private TermQueryBuilder isNewsitem() {
		return QueryBuilders.termQuery(TYPE, "N");
	}
	
	private TermQueryBuilder isWatchlist() {
		return QueryBuilders.termQuery(TYPE, "L");
	}

	private TermQueryBuilder isFeed() {
		return QueryBuilders.termQuery(TYPE, "F");
	}
	
	private void addShouldShowBrokenClause(BoolQueryBuilder query, boolean shouldShowBroken) {
		if (!shouldShowBroken) {
			final TermQueryBuilder contentIsOk = QueryBuilders.termQuery(HTTP_STATUS, "200");
			final TermQueryBuilder contentIsApproved = QueryBuilders.termQuery(HELD, false);			
			final BoolQueryBuilder contentIsPublic = QueryBuilders.boolQuery().must(contentIsOk).must(contentIsApproved);
			
			final BoolQueryBuilder userCanViewContent = QueryBuilders.boolQuery().minimumNumberShouldMatch(1).should(contentIsPublic);			
			if (loggedInUserFilter.getLoggedInUser() != null) {
				userCanViewContent.should(QueryBuilders.termQuery(OWNER, loggedInUserFilter.getLoggedInUser().getProfilename()));
			}
			
			query = query.must(userCanViewContent);
		}
		return;
	}

	private void addDateDescendingOrder(final SearchRequestBuilder searchRequestBuilder) {
		searchRequestBuilder.addSort(DATE, SortOrder.DESC);
		searchRequestBuilder.addSort(ID, SortOrder.DESC);
	}

	private void addNameOrder(final SearchRequestBuilder searchRequestBuilder) {
		searchRequestBuilder.addSort(NAME, SortOrder.ASC);
	}
	
	private void addLatestFeedItemOrder(SearchRequestBuilder searchRequestBuilder) {
		searchRequestBuilder.addSort(LATEST_ITEM_DATE, SortOrder.DESC);
	}
	
	private SearchRequestBuilder searchRequestBuilder() {
		return elasticSearchClientFactory.getClient().prepareSearch()
				.setIndices(ElasticSearchIndexUpdateService.INDEX).
				setTypes(ElasticSearchIndexUpdateService.TYPE);
	}
	
	private List<FrontendResource> deserializeFrontendResourceHits(SearchHits hits) {
		final List<FrontendResource> resources = Lists.newArrayList();
		final Iterator<SearchHit> iterator = hits.iterator();
		while (iterator.hasNext()) {
			final SearchHit next = iterator.next();
			try {
				final String typeField = next.getSource().get(TYPE).toString();
				if (typeField.equals("N")) {
					resources.add(objectMapper.readValue(next.getSourceAsString(), FrontendNewsitem.class));
				} else if (typeField.equals("F")) {
					resources.add(objectMapper.readValue(next.getSourceAsString(), FrontendFeed.class));
				} else {
					resources.add(objectMapper.readValue(next.getSourceAsString(), FrontendResource.class));
				}
				
			} catch (JsonParseException e) {
				throw new RuntimeException(e);
			} catch (JsonMappingException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return resources;
	}
	
}
