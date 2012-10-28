package nz.co.searchwellington.repositories;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nz.co.searchwellington.model.ArchiveLink;
import nz.co.searchwellington.model.PublisherContentCount;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.solr.SolrQueryBuilder;
import nz.co.searchwellington.repositories.solr.SolrQueryService;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.FacetParams;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class SolrBackedResourceDAO {
	
	private static Logger log = Logger.getLogger(SolrBackedResourceDAO.class);

	private static final int MAXIMUM_FEEDS = 500;
	private static final int MAXIMUM_ARCHIVE_MONTHS = 1000;
	private static final int MAXIMUM_PUBLISHERS_FACET_LIMIT = 2000;
	private static final int MAXIMUM_NEWSITEMS_ON_MONTH_ARCHIVE = 1000;
	private static final int TAG_TWITTER_OF_INTEREST_THRESHOLD = 2;
	
	private SolrQueryBuilderFactory solrQueryBuilderFactory;
	private SolrQueryService solrQueryService;
	private TagDAO tagDAO;
	private SolrResourceHydrator resourceHydrator;

	@Value("#{config['solr.url']}")
	private String solrUrl;

	@Autowired
	public SolrBackedResourceDAO(SolrQueryBuilderFactory solrQueryBuilderFactory, SolrQueryService solrQueryService, TagDAO tagDAO, SolrResourceHydrator resourceHydrator) {
		this.solrQueryBuilderFactory = solrQueryBuilderFactory;
		this.solrQueryService = solrQueryService;
		this.tagDAO = tagDAO;
		this.resourceHydrator = resourceHydrator;
	}
	
	public List<FrontendResource> getAllFeeds(boolean showBroken,
			boolean orderByLatestItemDate) {
		SolrQuery query = new SolrQueryBuilder().type("F").showBroken(
				showBroken).maxItems(MAXIMUM_FEEDS).toQuery();

		if (orderByLatestItemDate) {
			setFeedLatestItemOrder(query);
		} else {
			setTitleSortOrder(query);
		}
		return getQueryResults(query);
	}
	
	public int getTaggedNewsitemsCount(Set<Tag> tags, boolean shouldShowBroken) {
		SolrQuery query = new SolrQueryBuilder().type("N").tags(tags).showBroken(shouldShowBroken).toQuery();
		return getQueryCount(query);
	}
	
	public List<FrontendResource> getTaggedNewsitems(Set<Tag> name, boolean showBroken, int maxItems) {
		return getTaggedNewsitems(name, showBroken, 0, maxItems);
	}

	public int getTaggedNewitemsCount(Tag tag, boolean showBroken) {
		log.info("Getting newsitem count for tag: " + tag);
		Set<Tag> tags = new HashSet<Tag>();
		tags.add(tag);
		SolrQuery query = getTaggedContentSolrQuery(tags, showBroken, "N");
		return getQueryCount(query);
	}

	public FrontendNewsitem getNewspage(String pageUrl, boolean showBroken) {
		SolrQuery query = new SolrQueryBuilder().showBroken(showBroken).type(
				"N").pageUrl(pageUrl).toQuery();
		List<FrontendResource> queryResults = getQueryResults(query);
		log.info(queryResults.size()
				+ " results found while searching for page url: " + pageUrl);
		if (queryResults.size() == 1) {
			return (FrontendNewsitem) queryResults.get(0);
		}
		return null;
	}

	public int getCommentedNewsitemsCount(boolean showBroken) {
		log.info("Getting commented newsitem count");
		SolrQuery query = getCommentedNewsitemsQuery(showBroken);
		return getQueryCount(query);
	}

	public List<FrontendResource> getRecentCommentedNewsitemsForTag(Tag tag,
			boolean showBroken, int maxItems) {
		log.info("Getting recent commented newsitem count");
		// TODO duplication - with what?
		SolrQuery query = new SolrQueryBuilder().showBroken(showBroken).type(
				"N").tag(tag).dateRange(14).commented(true).toQuery();
		setDateDescendingOrder(query);
		query.setRows(maxItems);
		return getQueryResults(query);
	}

	public int getCommentedNewsitemsForTagCount(Tag tag, boolean showBroken) {
		log.info("Getting commented newsitem count for tag: " + tag);
		SolrQuery query = getCommentedNewsitemsForTagQuery(tag, showBroken);
		return getQueryCount(query);
	}

	public List<FrontendResource> getTaggedWebsites(Tag tag,
			boolean showBroken, int maxItems) {
		Set<Tag> tags = new HashSet<Tag>();
		tags.add(tag);
		return getTaggedWebsites(tags, showBroken, maxItems);
	}

	public List<FrontendResource> getTaggedGeotaggedNewsitems(Tag tag,
			int maxItems, boolean showBroken) {
		log.info("Getting geotagged newsitems for tag: " + tag);
		SolrQuery query = new SolrQueryBuilder().tag(tag).type("N").geotagged()
				.showBroken(showBroken).maxItems(maxItems).toQuery();
		setDateDescendingOrder(query);
		;
		return getQueryResults(query);
	}

	public List<FrontendResource> getTaggedWebsites(Set<Tag> tags,
			boolean showBroken, int maxItems) {
		log.info("Getting websites for tags: " + tags);
		SolrQuery query = new SolrQueryBuilder().tags(tags).type("W")
				.showBroken(showBroken).maxItems(maxItems).toQuery();
		setTitleSortOrder(query);
		return getQueryResults(query);
	}

	public Date getLastLiveTimeForTag(Tag tag) {
		SolrQuery latestItemForTagQuery = new SolrQueryBuilder().tag(tag)
				.showBroken(false).maxItems(1).toQuery();
		latestItemForTagQuery.setSortField("lastLive", ORDER.desc);
		List<FrontendResource> resources = getQueryResults(latestItemForTagQuery);
		if (resources.size() == 1) {
			return resources.get(0).getLiveTime(); // TODO Do this as a specific
													// API call, so that this
													// doesn't have to be
													// exposed on the frontend
													// resource model
		}
		return null;
	}

	public List<FrontendResource> getTagWatchlist(Tag tag, boolean showBroken) {
		log.info("Getting watchlist for tag: " + tag);
		SolrQuery query = new SolrQueryBuilder().tag(tag).type("L").showBroken(
				showBroken).toQuery();
		setTitleSortOrder(query);
		return getQueryResults(query);
	}

	public List<FrontendResource> getOwnedBy(User user, int maxItems, boolean showBroken) {
		SolrQuery query = new SolrQueryBuilder().owningUser(user).showBroken(showBroken).toQuery();
		setDateDescendingOrder(query);
		return getQueryResults(query);
	}
		
	public List<FrontendResource> getHandTaggingsForUser(User user, boolean showBroken) {
		SolrQuery query = new SolrQueryBuilder().taggingUser(user).showBroken(showBroken).toQuery();
		setDateDescendingOrder(query);
		return getQueryResults(query);
	}

	public List<FrontendResource> getLatestWebsites(int maxItems,
			boolean showBroken) {
		SolrQuery query = new SolrQueryBuilder().type("W").showBroken(
				showBroken).maxItems(maxItems).toQuery();
		setDateDescendingOrder(query);
		return getQueryResults(query);
	}

	public List<FrontendResource> getTaggedFeeds(Tag tag, boolean showBroken) {
		log.info("Getting feeds for tag: " + tag);
		SolrQuery query = new SolrQueryBuilder().tag(tag).type("F").showBroken(
				showBroken).toQuery();
		setTitleSortOrder(query);
		return getQueryResults(query);
	}

	public List<FrontendResource> getCommentedNewsitems(int maxItems,
			boolean showBroken, boolean hasComments, int startIndex) {
		SolrQuery query = getCommentedNewsitemsQuery(showBroken);
		setDateDescendingOrder(query);
		;
		query.setRows(maxItems);
		query.setStart(startIndex);
		return getQueryResults(query);
	}
	
	public int getTwitteredNewsitemsCount(boolean showBroken) {
		SolrQuery query = new SolrQueryBuilder().type("N").showBroken(showBroken).minTwitterCount(1).toQuery();
		return getQueryCount(query);
	}

	public List<FrontendResource> getTwitteredNewsitems(int startIndex, int maxItems, boolean showBroken) {
		SolrQuery query = new SolrQueryBuilder().type("N").showBroken(showBroken)
			.minTwitterCount(1).startIndex(startIndex).maxItems(maxItems)
			.toQuery();

		setDateDescendingOrder(query);
		return getQueryResults(query);
	}

	public List<FrontendResource> getRecentTwitteredNewsitemsForTag(
			int maxItems, boolean showBroken, Tag tag) {
		SolrQuery query = new SolrQueryBuilder().type("N").showBroken(
				showBroken).dateRange(60).tag(tag).minTwitterCount(
				TAG_TWITTER_OF_INTEREST_THRESHOLD).maxItems(maxItems).toQuery();

		setDateDescendingOrder(query);
		return getQueryResults(query);
	}

	public List<FrontendResource> getPublisherTagCombinerNewsitems(
			Website publisher, Tag tag, boolean showBroken, int maxItems) {
		SolrQuery query = new SolrQueryBuilder().showBroken(showBroken).type(
				"N").tag(tag).maxItems(maxItems).publisher(publisher).toQuery();
		setDateDescendingOrder(query);
		return getQueryResults(query);
	}

	public List<FrontendResource> getPublisherFeeds(Website publisher,
			boolean showBroken) {
		SolrQuery query = new SolrQueryBuilder().showBroken(showBroken).type(
				"F").publisher(publisher).toQuery();
		setTitleSortOrder(query);
		return getQueryResults(query);
	}

	public List<FrontendResource> getPublisherWatchlist(Website publisher,
			boolean showBroken) {
		SolrQuery query = new SolrQueryBuilder().showBroken(showBroken).type(
				"L").publisher(publisher).toQuery();
		setTitleSortOrder(query);
		return getQueryResults(query);
	}

	public List<FrontendResource> getCommentedNewsitemsForTag(Tag tag,
			boolean showBroken, int maxItems, int startIndex) {
		SolrQuery query = getCommentedNewsitemsForTagQuery(tag, showBroken);
		setDateDescendingOrder(query);
		;
		query.setStart(startIndex);
		query.setRows(maxItems);
		return getQueryResults(query);
	}

	public List<ArchiveLink> getArchiveMonths(boolean showBroken) {
		final SolrQuery query = new SolrQueryBuilder().showBroken(showBroken).type("N").toQuery();		
		query.addDateRangeFacet("date", new DateTime(1990, 1, 1, 0, 0).toDate(), DateTime.now().toDate(), "+1MONTH");
		query.setFacetMinCount(1);
		query.setFacetSort(FacetParams.FACET_SORT_INDEX);
		query.setFacetLimit(MAXIMUM_ARCHIVE_MONTHS);
		
		final List<ArchiveLink> archiveLinks = Lists.newArrayList();
		final QueryResponse response = solrQueryService.querySolr(query);
		if (response != null && response.getFacetRanges() != null) {
              for (RangeFacet rangeFacet : response.getFacetRanges()) {
                      for (RangeFacet.Count count : (List<RangeFacet.Count>) rangeFacet.getCounts()) {
                    	  final String dateRange = count.getValue();
          					DateTime month = new DateTime(dateRange);
                          	archiveLinks.add(new ArchiveLink(month.toDate(), count.getCount()));                          	
                      }
              }
		}
		
		Collections.reverse(archiveLinks);
		return archiveLinks;
	}

	public List<PublisherContentCount> getAllPublishers(boolean shouldShowBroken, boolean mustHaveNewsitems) {
		return getAllPublishersWithContentCounts(shouldShowBroken, mustHaveNewsitems);
	}
	
	public List<FrontendResource> getNewsitemsForMonth(Date month, boolean showBroken) {
		final SolrQueryBuilder solrQueryBuilder = solrQueryBuilderFactory.makeNewBuilder();
		final DateTime startOfMonth = new DateTime(new DateTime(month).toDateMidnight()).withDayOfMonth(1);
		final SolrQuery query = solrQueryBuilder.
			type("N").
			startDate(startOfMonth).
			endDate(startOfMonth.plusMonths(1)).
			showBroken(showBroken).
			toQuery();
		
		setDateDescendingOrder(query);	// TODO needs to be on the query builder.
		query.setRows(MAXIMUM_NEWSITEMS_ON_MONTH_ARCHIVE);
		return getQueryResults(query);
	}

	public int getPublisherNewsitemsCount(Website publisher, boolean showBroken) {
		log.info("Getting publisher newsitem count for publisher: "
				+ publisher.getName());
		SolrQuery query = new SolrQueryBuilder().showBroken(showBroken).type(
				"N").publisher(publisher).toQuery();
		return getQueryCount(query);
	}

	// TODO need a paginating method
	public List<FrontendResource> getAllWatchlists(boolean showBroken) {
		// TODO make this limit to things which changed in the last week.
		SolrQuery query = new SolrQueryBuilder().type("L").maxItems(255)
				.showBroken(showBroken).toQuery();
		query.setSortField("lastChanged", ORDER.desc);
		return getQueryResults(query);
	}

	public List<FrontendResource> getBrokenSites() {
		SolrQuery query = new SolrQueryBuilder().type("W").maxItems(255)
				.isBroken().toQuery();
		query.setSortField("titleSort", ORDER.asc);
		return getQueryResults(query);
	}

	public List<FrontendResource> getCalendarFeedsForTag(Tag tag,
			boolean showBroken) {
		SolrQuery query = new SolrQueryBuilder().type("C").showBroken(
				showBroken).toQuery();
		setTitleSortOrder(query);
		return getQueryResults(query);
	}

	public List<FrontendResource> getPublisherNewsitems(Website publisher,
			int maxItems, boolean showBroken) {
		return getPublisherNewsitems(publisher, maxItems, showBroken, 0);
	}

	public List<FrontendResource> getPublisherNewsitems(Website publisher,
			int maxItems, boolean showBroken, int startIndex) {
		SolrQuery query = new SolrQueryBuilder().type("N").publisher(publisher)
				.showBroken(showBroken).maxItems(maxItems).startIndex(
						startIndex).toQuery();
		setDateDescendingOrder(query);
		return getQueryResults(query);
	}

	public List<FrontendResource> getTaggedNewsitems(Set<Tag> tags, boolean showBroken, int startIndex, int maxItems) {
		log.info("Getting newsitems for tags: " + tags + " startIndex: " + startIndex + " maxItems: " + maxItems);
		SolrQuery query = new SolrQueryBuilder().type("N").tags(tags).showBroken(showBroken).startIndex(startIndex).maxItems(maxItems).toQuery();
		setDateDescendingOrder(query);
		;
		return getQueryResults(query);
	}

	public List<FrontendResource> getQueryResults(SolrQuery query) {
		log.debug("Solr query: " + query);
		QueryResponse response = solrQueryService.querySolr(query);
		if (response != null) {
			return loadResourcesFromSolrResults(response);
		}
		return Collections.emptyList();
	}

	public List<FrontendResource> getLatestNewsitems(int maxItems,
			boolean showBroken) {
		SolrQuery query = new SolrQueryBuilder().type("N").showBroken(
				showBroken).maxItems(maxItems).toQuery();
		setDateDescendingOrder(query);
		return getQueryResults(query);
	}

	public Map<String, Integer> getArchiveStatistics(boolean showBroken) { // TODO
																			// fails
																			// is
																			// admin
																			// user
																			// is
																			// logged
																			// in
																			// -
																			// because
																			// you
																			// can't
																			// facet
																			// on
																			// a
																			// null
																			// query
		SolrQuery query = new SolrQueryBuilder().allContentTypes().showBroken(
				showBroken).maxItems(0).toQuery();
		SolrServer solr;
		try {
			solr = new CommonsHttpSolrServer(solrUrl);	// TODO should be injected

			query.addFacetField("type");
			query.setFacetMinCount(1);

			QueryResponse response = solr.query(query);
			FacetField facetField = response.getFacetField("type");
			if (facetField != null && facetField.getValues() != null) {
				log.info("Found facet field: " + facetField);
				Map<String, Integer> typeCounts = new HashMap<String, Integer>();
				List<Count> values = facetField.getValues();
				for (Count count : values) {
					String type = (String) count.getName();
					final Long typeCount = count.getCount();
					typeCounts.put(type, typeCount.intValue());
				}
				return typeCounts;
			}
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (SolrServerException e) {
			log.error(e);
		}
		log.warn("returning null");
		return null;
	}

	public List<Tag> getCommentedTags(boolean showBroken) {
		final List<Integer> tagIds = Lists.newArrayList();
		try {
			SolrServer solr = new CommonsHttpSolrServer(solrUrl);
			SolrQuery query = getCommentedNewsitemsQuery(showBroken);
			query.addFacetField("tags");
			query.setFacetMinCount(1);
			query.setFacetSort(FacetParams.FACET_SORT_COUNT);

			QueryResponse response = solr.query(query);
			FacetField facetField = response.getFacetField("tags");
			if (facetField != null && facetField.getValues() != null) {
				log.debug("Found facet field: " + facetField);
				List<Count> values = facetField.getValues();
				for (Count count : values) {
					final int tagId = Integer.parseInt(count.getName());
					tagIds.add(tagId);
				}
			}

		} catch (MalformedURLException e) {
			log.error(e);
		} catch (SolrServerException e) {
			log.error(e);
		}
		return tagDAO.loadTagsById(tagIds);
	}

	public List<FrontendResource> getValidGeotagged(int startIndex, int maxItems, boolean showBroken) {
		SolrQuery query = new SolrQueryBuilder().type("N").showBroken(true)
				.geotagged().startIndex(startIndex).maxItems(maxItems)
				.toQuery();
		setDateDescendingOrder(query);
		return getQueryResults(query);
	}

	public List<FrontendResource> getGeotaggedNewsitemsNear(double latitude, double longitude, double radius, boolean showBroken, int startIndex, int maxNewsitems) {
		SolrQuery query = new SolrQueryBuilder().toNewsitemsNearQuery(latitude, longitude, radius, showBroken, startIndex, maxNewsitems);
		setDateDescendingOrder(query);
		return getQueryResults(query);
	}

	public int getGeotaggedNewsitemsNearCount(double latitude, double longitude, double radius, boolean showBroken) {
		SolrQuery query = new SolrQueryBuilder().toNewsitemsNearQuery(latitude, longitude, radius, showBroken, 0, 0); // TODO maxitems not ideal
		return getQueryCount(query);
	}

	public int getGeotaggedCount(boolean shouldShowBroken) {
		SolrQuery query = new SolrQueryBuilder().type("N").showBroken(true)
				.geotagged().toQuery();
		return getQueryCount(query);
	}

	public List<Tag> getGeotaggedTags(boolean showBroken) {
		List<Integer> tagIds = Lists.newArrayList();
		try {
			SolrServer solr = new CommonsHttpSolrServer(solrUrl);
			SolrQuery query = new SolrQueryBuilder().type("N").showBroken(
					showBroken).geotagged().toQuery();
			query.addFacetField("tags");
			query.setFacetMinCount(1);
			query.setFacetSort(FacetParams.FACET_SORT_COUNT);

			QueryResponse response = solr.query(query);
			FacetField facetField = response.getFacetField("tags");
			if (facetField != null && facetField.getValues() != null) {
				log.debug("Found facet field: " + facetField);
				List<Count> values = facetField.getValues();
				for (Count count : values) {
					final int tagId = Integer.parseInt(count.getName());
					tagIds.add(tagId);
				}
			}

		} catch (MalformedURLException e) {
			log.error(e);
		} catch (SolrServerException e) {
			log.error(e);
		}
		return tagDAO.loadTagsById(tagIds);
	}

	// TODO nothing actually uses the counts; only using the sorting
	// functionaility - could remove
	private List<PublisherContentCount> getAllPublishersWithContentCounts(boolean showBroken, boolean mustHaveNewsitems) {
		SolrQuery query = new SolrQueryBuilder().allPublishedTypes().showBroken(showBroken).toQuery();
		query.addFacetField(SolrInputDocumentBuilder.PUBLISHER_NAME);
		query.setFacetMinCount(1);
		query.setFacetSort(FacetParams.FACET_SORT_INDEX);
		query.setFacetLimit(MAXIMUM_PUBLISHERS_FACET_LIMIT);

		List<PublisherContentCount> publishers = Lists.newArrayList();
		QueryResponse response = solrQueryService.querySolr(query);
		if (response != null) {
			FacetField facetField = response.getFacetField(SolrInputDocumentBuilder.PUBLISHER_NAME);
			if (facetField != null && facetField.getValues() != null) {
				log.debug("Found facet field: " + facetField);
				List<Count> values = facetField.getValues();
				for (Count count : values) {
					final String relatedPublisherName =  (String) count.getName();	// TODO duplication		
					final Long relatedItemCount = count.getCount();					
					publishers.add(new PublisherContentCount(relatedPublisherName, relatedItemCount.intValue()));					
				}
			}
		}
		Collections.sort(publishers);
		return publishers;
	}

	private List<FrontendResource> loadResourcesFromSolrResults(QueryResponse response) {
		List<FrontendResource> results = Lists.newArrayList();
		SolrDocumentList solrResults = response.getResults();
		for (SolrDocument result : solrResults) {
			FrontendResource resource = resourceHydrator
					.hydrateResource(result);
			if (resource != null) {
				results.add(resource);
			}
		}
		return results;
	}

	public int getQueryCount(SolrQuery query) {
		QueryResponse response = solrQueryService.querySolr(query);
		if (response != null) {
			Long count = response.getResults().getNumFound();
			return count.intValue();
		}
		return 0;
	}

	private SolrQuery getCommentedNewsitemsQuery(boolean showBroken) {
		return new SolrQueryBuilder().showBroken(showBroken).type("N")
				.commented(true).toQuery();
	}

	private SolrQuery getCommentedNewsitemsForTagQuery(Tag tag,
			boolean showBroken) {
		return new SolrQueryBuilder().showBroken(showBroken).type("N").tag(tag)
				.commented(true).toQuery();
	}

	private SolrQuery getTaggedContentSolrQuery(Set<Tag> tags,
			boolean showBroken, String type) {
		return new SolrQueryBuilder().tags(tags).showBroken(showBroken).type(
				type).toQuery();
	}

	private void setDateDescendingOrder(SolrQuery query) {
		query.setSortField("date", ORDER.desc);
		query.addSortField("id", ORDER.desc);
	}

	private void setTitleSortOrder(SolrQuery query) {
		query.setSortField("titleSort", ORDER.asc);
	}

	private void setFeedLatestItemOrder(SolrQuery query) {
		query.setSortField("feedLatestItemDate", ORDER.desc);
	}
	
}
