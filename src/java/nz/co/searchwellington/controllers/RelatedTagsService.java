package nz.co.searchwellington.controllers;

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
	
		
	public TagRelatedLinks getTagsRelatedLinks(Tag tag, boolean showBroken, int maxItems) {
		TagRelatedLinks relatedLinks = new TagRelatedLinks();
	
		SolrQuery query = new SolrQueryBuilder().tag(tag).showBroken(showBroken).toQuery();
		query.addFacetField("tags");
		query.addFacetField("publisher");
		query.setFacetMinCount(1);
		
		Map<String, List<Count>> facetResuls = solrQueryService.getFacetQueryResults(query);		
		relatedLinks.setRelatedTags(solrFacetLoader.getRelatedTagLinks(facetResuls.get("tags"), tag));
		relatedLinks.setRelatedPublisers(solrFacetLoader.getRelatedPublisherLinks(facetResuls.get("publisher")));
		return relatedLinks;
	}
	
	public List<TagContentCount> getRelatedTagLinks(Website publisher, boolean showBroken) {
		SolrQuery query = new SolrQueryBuilder().publisher(publisher).showBroken(showBroken).toQuery();
		query.addFacetField("tags");
		query.setFacetMinCount(1);
		
		Map<String, List<Count>> facetResuls = solrQueryService.getFacetQueryResults(query);
		return solrFacetLoader.getRelatedTagLinks(facetResuls.get("tags"), null);
	}
	
}
