package nz.co.searchwellington.tagging;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.HandTagging;
import nz.co.searchwellington.model.PublishedResource;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TaggingVote;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.HandTaggingDAO;

import org.apache.log4j.Logger;

public class TaggingReturnsOfficerService {
		
	static Logger log = Logger.getLogger(TaggingReturnsOfficerService.class);

	private HandTaggingDAO tagVoteDAO;

	
	public TaggingReturnsOfficerService(HandTaggingDAO tagVoteDAO) {
		this.tagVoteDAO = tagVoteDAO;
	}

		
	public Set<Tag> getHandTagsForResource(Resource resource) {		
		Set<Tag>tags = new HashSet<Tag>();
		List<HandTagging> handTaggings = tagVoteDAO.getHandTaggingsForResource(resource);			
		for (HandTagging tagging : handTaggings) {
			tags.add(tagging.getTag());
		}
		return tags;		
	}
	
	
	public Set<Tag> getIndexTagsForResource(Resource resource) {		
		List<TaggingVote> taggingVotes = complieTaggingVotes(resource);			
		Set <Tag> indexTags = new HashSet<Tag>();
		for (TaggingVote vote : taggingVotes) {
			if (!indexTags.contains(vote.getTag())) {
				indexTags.add(vote.getTag());
			}
		}
		return indexTags;
	}


	public List<TaggingVote> complieTaggingVotes(Resource resource) {
		List<TaggingVote> votes = new ArrayList<TaggingVote>();
		for (HandTagging handTagging : tagVoteDAO
				.getHandTaggingsForResource(resource)) {
			votes.add(new TaggingVote(handTagging.getTag(),
					handTagging.getUser(), 100));
		}

		final boolean shouldAppearOnPublisherAndParentTagPages = resource
				.getType().equals("L")
				|| resource.getType().equals("N")
				|| resource.getType().equals("C")
				|| resource.getType().equals("F");

		if (shouldAppearOnPublisherAndParentTagPages) {
			addAncestorTagVotes(resource, votes);
			addPublisherDerviedTags(resource, votes);
		}
		return votes;
	}


	private void addPublisherDerviedTags(Resource resource,
			List<TaggingVote> votes) {
		if (((PublishedResource) resource).getPublisher() != null) {
			Website publisher = ((PublishedResource) resource)
					.getPublisher();
			for (Tag publisherTag : this.getHandTagsForResource(publisher)) {
				votes.add(new TaggingVote(publisherTag,
						new PublishersTagsVoter(), 100));
				for (Tag publishersAncestor : publisherTag.getAncestors()) {
					votes.add(new TaggingVote(publishersAncestor,
							new PublishersTagAncestorTagVoter(), 100));
				}
			}
		}
	}


	private void addAncestorTagVotes(Resource resource, List<TaggingVote> votes) {
		for (Tag tag : this.getHandTagsForResource(resource)) {
			for (Tag ancestorTag: tag.getAncestors()) {
				votes.add(new TaggingVote(ancestorTag, new AncestorTagVoter(), 100));
			}
		}
	}

}
