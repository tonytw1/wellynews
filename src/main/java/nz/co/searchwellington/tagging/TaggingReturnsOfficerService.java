package nz.co.searchwellington.tagging;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.HandTagging;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.PublishedResource;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.taggingvotes.GeotaggingVote;
import nz.co.searchwellington.model.taggingvotes.TaggingVote;
import nz.co.searchwellington.model.taggingvotes.voters.AncestorTagVoter;
import nz.co.searchwellington.model.taggingvotes.voters.FeedTagAncestorTagVoter;
import nz.co.searchwellington.model.taggingvotes.voters.FeedsTagsTagVoter;
import nz.co.searchwellington.model.taggingvotes.voters.PublishersTagAncestorTagVoter;
import nz.co.searchwellington.model.taggingvotes.voters.PublishersTagsVoter;
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
		Set <Tag> indexTags = new HashSet<Tag>();
		for (TaggingVote vote : complieTaggingVotes(resource)) {
			if (!indexTags.contains(vote.getTag())) {
				indexTags.add(vote.getTag());
			}
		}
		return indexTags;
	}
	
	public Geocode getIndexGeocodeForResource(Resource resource) {
		List<GeotaggingVote> votes = getGeotagVotesForResource(resource);
		if (!votes.isEmpty()) {
			return votes.get(0).getGeotag();
		}
		return null;
	}
	
	public List<GeotaggingVote> getGeotagVotesForResource(Resource resource) {
		List<GeotaggingVote> votes = new ArrayList<GeotaggingVote>();
		if (resource.getGeocode() != null && resource.getGeocode().isValid()) {
			votes.add(new GeotaggingVote(resource.getGeocode(), resource.getOwner(), 1));
		}
		
		Geocode tagGeocode = getGeotagFromFirstResourceTagWithLocation(getIndexTagsForResource(resource));	// TODO could be made faster by passing in.
		if (tagGeocode != null && tagGeocode.isValid()) {
			votes.add(new GeotaggingVote(tagGeocode, new AncestorTagVoter(), 1));	// TODO needs a new voter type
		}
		return votes;
	}
	
	public List<TaggingVote> complieTaggingVotes(Resource resource) {
		List<TaggingVote> votes = new ArrayList<TaggingVote>();
		for (HandTagging handTagging : tagVoteDAO.getHandTaggingsForResource(resource)) {
			votes.add(new TaggingVote(handTagging.getTag(), handTagging.getUser(), 100));
		}

		final boolean shouldAppearOnPublisherAndParentTagPages = resource.getType().equals("L")
				|| resource.getType().equals("N")
				|| resource.getType().equals("C")
				|| resource.getType().equals("F");

		if (shouldAppearOnPublisherAndParentTagPages) {
			addAncestorTagVotes(resource, votes);
			addPublisherDerviedTags(resource, votes);
		}
		
		if (resource.getType().equals("N")) {
			Feed acceptedFeed = ((Newsitem) resource).getFeed();
			if (acceptedFeed != null) {
				addAcceptedFromFeedTags(resource, this.getHandTagsForResource(acceptedFeed), votes);
			}
		}
				
		return votes;
	}
	
	private void addAcceptedFromFeedTags(Resource resource, Set<Tag> feedsHandTags, List<TaggingVote> votes) {
		for (Tag tag : feedsHandTags) {
			votes.add(new TaggingVote(tag, new FeedsTagsTagVoter(), 100));
			for (Tag feedTagAncestor : tag.getAncestors()) {
				votes.add(new TaggingVote(feedTagAncestor, new FeedTagAncestorTagVoter(), 100));
			}
		}
				
	}
	
	private void addPublisherDerviedTags(Resource resource,
			List<TaggingVote> votes) {
		if (((PublishedResource) resource).getPublisher() != null) {
			Website publisher = ((PublishedResource) resource).getPublisher();
			for (Tag publisherTag : this.getHandTagsForResource(publisher)) {
				votes.add(new TaggingVote(publisherTag, new PublishersTagsVoter(), 100));
				for (Tag publishersAncestor : publisherTag.getAncestors()) {
					votes.add(new TaggingVote(publishersAncestor, new PublishersTagAncestorTagVoter(), 100));
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
	
	private Geocode getGeotagFromFirstResourceTagWithLocation(Set<Tag> indexTagsForResource) {
		for (Tag tag : indexTagsForResource) {
			if (tag.getGeocode() != null && tag.getGeocode().isValid()) {
				log.info("Found subsitute geotag for resource on resource index tag: " + tag.getName());
				return tag.getGeocode();
			}
		}
		return null;
	}
	
}
