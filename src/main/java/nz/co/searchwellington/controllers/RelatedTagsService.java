package nz.co.searchwellington.controllers;

import java.util.List;
import java.util.Map;

import nz.co.searchwellington.controllers.models.GeotaggedModelBuilder;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.PublisherContentCount;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.SolrInputDocumentBuilder;
import nz.co.searchwellington.repositories.solr.SolrFacetLoader;
import nz.co.searchwellington.repositories.solr.SolrKeywordQueryBuilder;
import nz.co.searchwellington.repositories.solr.SolrQueryBuilder;
import nz.co.searchwellington.repositories.solr.SolrQueryService;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class RelatedTagsService {
	
	private static Logger log = Logger.getLogger(GeotaggedModelBuilder.class);
	
	private SolrQueryService solrQueryService;
	private SolrKeywordQueryBuilder solrKeywordQueryBuilder;
	private SolrFacetLoader solrFacetLoader;
	private ShowBrokenDecisionService showBrokenDecisionService;
	
	@Autowired
	public RelatedTagsService(SolrQueryService solrQueryService,
			SolrKeywordQueryBuilder solrKeywordQueryBuilder,
			SolrFacetLoader solrFacetLoader,
			ShowBrokenDecisionService showBrokenDecisionService) {
		this.solrQueryService = solrQueryService;
		this.solrKeywordQueryBuilder = solrKeywordQueryBuilder;
		this.solrFacetLoader = solrFacetLoader;
		this.showBrokenDecisionService = showBrokenDecisionService;
	}

	public List<TagContentCount> getRelatedLinksForTag(Tag tag, int maxItems) {	
		Map<String, List<Count>> facetResults = queryForRelatedTagAndPublisherFacets(tag, showBrokenDecisionService.shouldShowBroken());		
		List<TagContentCount> loadedTagFacet = solrFacetLoader.loadTagFacet(facetResults.get("tags"));
		List<TagContentCount> allFacets = removeUnsuitableTags(tag, loadedTagFacet);
		if (allFacets.size() > maxItems) {
			return allFacets.subList(0, maxItems);
		}
		return allFacets;		
	}
	
	public List<TagContentCount> getKeywordSearchFacets(String keywords, Tag tag) {
		SolrQuery query = solrKeywordQueryBuilder.getSolrKeywordQuery(keywords, showBrokenDecisionService.shouldShowBroken(), tag);			
		query.setRows(30);
		query.addFacetField("tags");
		query.setFacetMinCount(1);
		
		Map<String, List<Count>> facetQueryResults = solrQueryService.getFacetQueryResults(query);				
		List<TagContentCount> relatedTagLinks = solrFacetLoader.loadTagFacet(facetQueryResults.get("tags"));
		return relatedTagLinks;
	}
	
	public List<TagContentCount> getRelatedTagsForLocation(Geocode location, double radius, int maxItems) {
		log.info("Querying for location related tags: " + location.getAddress());
		Map<String, List<Count>> facetResults = queryForLocationRelatedTagAndPublisherFacets(location, radius, showBrokenDecisionService.shouldShowBroken());
		List<TagContentCount> loadedTagFacet = solrFacetLoader.loadTagFacet(facetResults.get("tags"));
		log.info("Found facet count: " + loadedTagFacet.size());
		if (loadedTagFacet.size() > maxItems) {
			return loadedTagFacet.subList(0, maxItems);
		}
		return loadedTagFacet;
	}
	
	public List<PublisherContentCount> getRelatedPublishersForLocation(Geocode location, double radius, int maxItems) {
		log.info("Querying for location related publishers: " + location.getAddress());
		Map<String, List<Count>> facetResults = queryForLocationRelatedTagAndPublisherFacets(location, radius, showBrokenDecisionService.shouldShowBroken());
		List<PublisherContentCount> loadedFacet = solrFacetLoader.loadPublisherFacet(facetResults.get(SolrInputDocumentBuilder.PUBLISHER_NAME));
		log.info("Found facet count: " + loadedFacet.size());
		if (loadedFacet.size() > maxItems) {
			return loadedFacet.subList(0, maxItems);
		}
		return loadedFacet;	
	}
	
	public List<PublisherContentCount> getRelatedPublishersForTag(Tag tag, int maxItems) {
		Map<String, List<Count>> facetResults = queryForRelatedTagAndPublisherFacets(tag, showBrokenDecisionService.shouldShowBroken());
		List<PublisherContentCount> allFacets = solrFacetLoader.loadPublisherFacet(facetResults.get(SolrInputDocumentBuilder.PUBLISHER_NAME));
		if (allFacets.size() > maxItems) {
			return allFacets.subList(0, maxItems);
		}
		return allFacets;	
	}
	
	public List<TagContentCount> getRelatedLinksForPublisher(Website publisher) {
		SolrQuery query = new SolrQueryBuilder().publisher(publisher).showBroken(showBrokenDecisionService.shouldShowBroken()).maxItems(0).toQuery();
		query.addFacetField("tags");
		query.setFacetMinCount(1);
		
		Map<String, List<Count>> facetResults = solrQueryService.getFacetQueryResults(query);
		return solrFacetLoader.loadTagFacet(facetResults.get("tags"));
	}
	
	public List<TagContentCount> getFeedworthyTags(boolean shouldShowBroken) {
		SolrQuery query = new SolrQueryBuilder().type("N").showBroken(shouldShowBroken).dateRange(90).maxItems(0).toQuery();
		query.addFacetField("tags");
		query.setFacetMinCount(10);
		Map<String, List<Count>> facetResults = solrQueryService.getFacetQueryResults(query);
		return solrFacetLoader.loadTagFacet(facetResults.get("tags"));
	}
	
	private Map<String, List<Count>> queryForRelatedTagAndPublisherFacets(Tag tag, boolean showBroken) {
		SolrQuery query = new SolrQueryBuilder().tag(tag).showBroken(showBroken).maxItems(0).toQuery();
		query.addFacetField("tags");
		query.addFacetField(SolrInputDocumentBuilder.PUBLISHER_NAME);
		query.setFacetMinCount(1);
		
		Map<String, List<Count>> facetResults = solrQueryService.getFacetQueryResults(query);		
		return facetResults;
	}

	private Map<String, List<Count>> queryForLocationRelatedTagAndPublisherFacets(Geocode location, double radius, boolean showBroken) {		
		final SolrQuery query = new SolrQueryBuilder().type("N").
		near(location.getLatitude(), location.getLongitude(), radius).
		showBroken(showBroken).
		startIndex(0).maxItems(1500).toQuery();
		
		query.addFacetField("tags");
		query.addFacetField(SolrInputDocumentBuilder.PUBLISHER_NAME);
		query.setFacetMinCount(1);	
		Map<String, List<Count>> facetResults = solrQueryService.getFacetQueryResults(query);
		return facetResults;
	}
	
	private List<TagContentCount> removeUnsuitableTags(Tag tag, List<TagContentCount> loadedTagFacet) {
		List<TagContentCount> suitableTagFacets = Lists.newArrayList();
		for (TagContentCount count : loadedTagFacet) {			
			if (isTagSuitableRelatedTag(tag, count.getTag())) {
				suitableTagFacets.add(count);
			}
		}
		return suitableTagFacets;
	}
	
	private boolean isTagSuitableRelatedTag(Tag tag, Tag relatedTag) {	
		return !relatedTag.isHidden() && !tag.equals(relatedTag) && !relatedTag.isParentOf(tag) && 
			!tag.getAncestors().contains(relatedTag) && !tag.getChildren().contains(relatedTag) &&
			!relatedTag.getName().equals("places") && !relatedTag.getName().equals("blogs");	// TODO push up
	}
	
}
