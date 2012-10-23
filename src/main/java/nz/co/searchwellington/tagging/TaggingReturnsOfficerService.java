package nz.co.searchwellington.tagging;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.PublishedResource;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.taggingvotes.GeneratedTaggingVote;
import nz.co.searchwellington.model.taggingvotes.GeotaggingVote;
import nz.co.searchwellington.model.taggingvotes.HandTagging;
import nz.co.searchwellington.model.taggingvotes.TaggingVote;
import nz.co.searchwellington.model.taggingvotes.voters.AncestorTagVoter;
import nz.co.searchwellington.model.taggingvotes.voters.FeedTagAncestorTagVoter;
import nz.co.searchwellington.model.taggingvotes.voters.FeedsTagsTagVoter;
import nz.co.searchwellington.model.taggingvotes.voters.PublishersTagAncestorTagVoter;
import nz.co.searchwellington.model.taggingvotes.voters.PublishersTagsVoter;
import nz.co.searchwellington.repositories.HandTaggingDAO;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Component
public class TaggingReturnsOfficerService {
		
	private static Logger log = Logger.getLogger(TaggingReturnsOfficerService.class);

	private HandTaggingDAO tagVoteDAO;
	
	@Autowired
	public TaggingReturnsOfficerService(HandTaggingDAO tagVoteDAO) {
		this.tagVoteDAO = tagVoteDAO;
	}
	
	public Set<Tag> getHandTagsForResource(Resource resource) {		
		final Set<Tag>tags = Sets.newHashSet();
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
		final List<GeotaggingVote> votes = Lists.newArrayList();
		if (resource.getGeocode() != null && resource.getGeocode().isValid()) {
			votes.add(new GeotaggingVote(resource.getGeocode(), resource.getOwner(), 1));
		}
		
		if (resource.getType().equals("N")) {
			if (((PublishedResource) resource).getPublisher() != null) {
				Website publisher = ((PublishedResource) resource).getPublisher();
				if (publisher.getGeocode() != null && publisher.getGeocode().isValid()) {
					log.info("Adding publisher geotag: " + publisher.getGeocode().toString());
					votes.add(new GeotaggingVote(publisher.getGeocode(), new PublishersTagsVoter(), 1));
				}
			}
		}
		
		Geocode tagGeocode = getGeotagFromFirstResourceTagWithLocation(getIndexTagsForResource(resource));	// TODO could be made faster by passing in.
		if (tagGeocode != null && tagGeocode.isValid()) {
			votes.add(new GeotaggingVote(tagGeocode, new AncestorTagVoter(), 1));	// TODO needs a new voter type
		}
		
		return votes;
	}
	
	public List<TaggingVote> complieTaggingVotes(Resource resource) {
		final List<TaggingVote> votes = Lists.newArrayList();
		for (HandTagging handTagging : tagVoteDAO.getHandTaggingsForResource(resource)) {
			votes.add(handTagging);
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
			votes.add(new GeneratedTaggingVote(tag, new FeedsTagsTagVoter()));
			for (Tag feedTagAncestor : tag.getAncestors()) {
				votes.add(new GeneratedTaggingVote(feedTagAncestor, new FeedTagAncestorTagVoter()));
			}
		}				
	}
	
	private void addPublisherDerviedTags(Resource resource, List<TaggingVote> votes) {
		if (((PublishedResource) resource).getPublisher() != null) {
			Website publisher = ((PublishedResource) resource).getPublisher();
			for (Tag publisherTag : this.getHandTagsForResource(publisher)) {
				votes.add(new GeneratedTaggingVote(publisherTag, new PublishersTagsVoter()));
				for (Tag publishersAncestor : publisherTag.getAncestors()) {
					votes.add(new GeneratedTaggingVote(publishersAncestor, new PublishersTagAncestorTagVoter()));
				}
			}						
		}
	}
	
	private void addAncestorTagVotes(Resource resource, List<TaggingVote> votes) {
		for (Tag tag : this.getHandTagsForResource(resource)) {
			for (Tag ancestorTag: tag.getAncestors()) {
				votes.add(new GeneratedTaggingVote(ancestorTag, new AncestorTagVoter()));
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
