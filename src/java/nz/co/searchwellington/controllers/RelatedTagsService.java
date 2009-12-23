package nz.co.searchwellington.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.solr.SolrFacetLoader;
import nz.co.searchwellington.repositories.solr.SolrQueryBuilder;
import nz.co.searchwellington.repositories.solr.SolrQueryService;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField.Count;

public class RelatedTagsService {
		
    Logger log = Logger.getLogger(RelatedTagsService.class);

	
	private SolrQueryService solrQueryService;
	private SolrFacetLoader solrFacetLoader;
	
    
	public RelatedTagsService(SolrQueryService solrQueryService, SolrFacetLoader solrFacetLoader) {		
		this.solrQueryService = solrQueryService;
		this.solrFacetLoader = solrFacetLoader;
	}
	
		
	public TagRelatedLinks getRelatedLinksForTag(Tag tag, boolean showBroken, int maxItems) {
		TagRelatedLinks relatedLinks = new TagRelatedLinks();
	
		SolrQuery query = new SolrQueryBuilder().tag(tag).showBroken(showBroken).toQuery();
		query.addFacetField("tags");
		query.addFacetField("publisher");
		query.setFacetMinCount(1);
		
		Map<String, List<Count>> facetResults = solrQueryService.getFacetQueryResults(query);		
		
		List<TagContentCount> loadedTagFacet = solrFacetLoader.loadTagFacet(facetResults.get("tags"));
		relatedLinks.setRelatedTags(removeUnsuitableTags(tag, loadedTagFacet));
		
		// TODO why don't we just do this in two calls?
		relatedLinks.setRelatedPublisers(solrFacetLoader.loadPublisherFacet(facetResults.get("publisher")));
		return relatedLinks;
	}
	

	public List<TagContentCount> getRelatedLinksForPublisher(Website publisher, boolean showBroken) {
		SolrQuery query = new SolrQueryBuilder().publisher(publisher).showBroken(showBroken).toQuery();
		query.addFacetField("tags");
		query.setFacetMinCount(1);
		
		Map<String, List<Count>> facetResuls = solrQueryService.getFacetQueryResults(query);
		return solrFacetLoader.loadTagFacet(facetResuls.get("tags"));
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
			!tag.getAncestors().contains(relatedTag) && !tag.getChildren().contains(tag);
	}
	
}
