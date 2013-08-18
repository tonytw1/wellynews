package nz.co.searchwellington.controllers;	// TODO move out of controllers package

import java.util.List;
import java.util.Map;

import nz.co.searchwellington.model.PublisherContentCount;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.frontend.FrontendTag;
import nz.co.searchwellington.repositories.TagDAO;
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchBackedResourceDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.geo.model.Place;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Component
public class RelatedTagsService {
		
	private final ElasticSearchBackedResourceDAO elasticSearchBackedResourceDAO;
	private final TagDAO tagDAO;
	
	@Autowired
	public RelatedTagsService(ElasticSearchBackedResourceDAO elasticSearchBackedResourceDAO, TagDAO tagDAO) {
		this.elasticSearchBackedResourceDAO = elasticSearchBackedResourceDAO;
		this.tagDAO = tagDAO;
	}

	public List<TagContentCount> getRelatedLinksForTag(Tag tag, int maxItems) {
		final Map<String, Integer> tagFacetsForTag = elasticSearchBackedResourceDAO.getTagFacetsForTag(tag);		
		final List<TagContentCount> filtered = removeUnsuitableTags(tag, tagFacetsForTag);
		return Lists.newArrayList(Iterables.limit(filtered, 5));
	}
	
	public List<TagContentCount> getKeywordSearchFacets(String keywords, Tag tag) {
		return Lists.newArrayList();	// TODO implement
	}
	
	public List<TagContentCount> getRelatedTagsForLocation(Place place, double radius, int maxItems) {
		return Lists.newArrayList();	// TODO implement
	}
	
	public List<PublisherContentCount> getRelatedPublishersForLocation(Place place, double radius, int maxItems) {
		return Lists.newArrayList();	// TODO implement
	}
	
	public List<PublisherContentCount> getRelatedPublishersForTag(Tag tag, int maxItems) {
		final Map<String, Integer> publisherFacetsForTag = elasticSearchBackedResourceDAO.getPublisherFacetsForTag(tag);
		final List<PublisherContentCount> publisherFacets = Lists.newArrayList();
		for (String publisher : publisherFacetsForTag.keySet()) {
			publisherFacets.add(new PublisherContentCount(publisher, publisherFacetsForTag.get(publisher)));
		}
		return publisherFacets;
	}
	
	public List<TagContentCount> getRelatedLinksForPublisher(Website publisher) {
		return Lists.newArrayList();	// TODO implement
	}
	
	public List<TagContentCount> getFeedworthyTags(boolean shouldShowBroken) {
		return Lists.newArrayList();	// TODO implement
	}
		
	private List<TagContentCount> removeUnsuitableTags(Tag tag, Map<String, Integer> tagFacetsForTag) {
		List<TagContentCount> suitableTagFacets = Lists.newArrayList();
		for (String tagId : tagFacetsForTag.keySet()) {
			final Tag facetTag = tagDAO.loadTagByName(tagId);	// TODO null safe
			if (isTagSuitableRelatedTag(tag, facetTag)) {
				FrontendTag frontendTag = new FrontendTag();
				frontendTag.setId(facetTag.getName());
				frontendTag.setName(facetTag.getDisplayName());
				suitableTagFacets.add(new TagContentCount(frontendTag, tagFacetsForTag.get(tagId)));
			}
		}
		return suitableTagFacets;
	}

	private boolean isTagSuitableRelatedTag(Tag tag, Tag relatedTag) {
		return !relatedTag.isHidden() && !tag.equals(relatedTag)
				&& !relatedTag.isParentOf(tag)
				&& !tag.getAncestors().contains(relatedTag)
				&& !tag.getChildren().contains(relatedTag)
				&& !relatedTag.getName().equals("places")
				&& !relatedTag.getName().equals("blogs"); // TODO push up
	}
	
}
