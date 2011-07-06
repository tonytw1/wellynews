package nz.co.searchwellington.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nz.co.searchwellington.model.PublisherContentCount;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.solr.SolrFacetLoader;
import nz.co.searchwellington.repositories.solr.SolrQueryBuilder;
import nz.co.searchwellington.repositories.solr.SolrQueryService;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField.Count;

public class RelatedTagsService {
	
	private SolrQueryService solrQueryService;
	private SolrFacetLoader solrFacetLoader;
	    
	public RelatedTagsService(SolrQueryService solrQueryService, SolrFacetLoader solrFacetLoader) {		
		this.solrQueryService = solrQueryService;
		this.solrFacetLoader = solrFacetLoader;
	}
	
	public List<TagContentCount> getRelatedLinksForTag(Tag tag, boolean showBroken, int maxItems) {	
		Map<String, List<Count>> facetResults = queryForRelatedTagAndPublisherFacets(tag, showBroken);		
		List<TagContentCount> loadedTagFacet = solrFacetLoader.loadTagFacet(facetResults.get("tags"));
		List<TagContentCount> allFacets = removeUnsuitableTags(tag, loadedTagFacet);
		if (allFacets.size() > maxItems) {
			return allFacets.subList(0, maxItems);
		}
		return allFacets;		
	}
	
	public List<PublisherContentCount> getRelatedPublishersForTag(Tag tag, boolean showBroken, int maxItems) {
		Map<String, List<Count>> facetResults = queryForRelatedTagAndPublisherFacets(tag, showBroken);		
		List<PublisherContentCount> allFacets = solrFacetLoader.loadPublisherFacet(facetResults.get("publisher"));
		if (allFacets.size() > maxItems) {
			return allFacets.subList(0, maxItems);
		}
		return allFacets;	
	}
	
	public List<TagContentCount> getRelatedLinksForPublisher(Website publisher, boolean showBroken) {
		SolrQuery query = new SolrQueryBuilder().publisher(publisher).showBroken(showBroken).maxItems(0).toQuery();
		query.addFacetField("tags");
		query.setFacetMinCount(1);
		
		Map<String, List<Count>> facetResuls = solrQueryService.getFacetQueryResults(query);
		return solrFacetLoader.loadTagFacet(facetResuls.get("tags"));
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
		query.addFacetField("publisher");
		query.setFacetMinCount(1);
		
		Map<String, List<Count>> facetResults = solrQueryService.getFacetQueryResults(query);
		return facetResults;
	}
	
	private List<TagContentCount> removeUnsuitableTags(Tag tag, List<TagContentCount> loadedTagFacet) {
		List<TagContentCount> suitableTagFacets = new ArrayList<TagContentCount>();
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
