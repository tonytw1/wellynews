package nz.co.searchwellington.tagging;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nz.co.searchwellington.model.PublishedResource;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TaggingVote;
import nz.co.searchwellington.repositories.TagVoteDAO;

import org.apache.log4j.Logger;

public class TaggingReturnsOfficerService {
		
	static Logger log = Logger.getLogger(TaggingReturnsOfficerService.class);

	private TagVoteDAO tagVoteDAO;

	
	public TaggingReturnsOfficerService(TagVoteDAO tagVoteDAO) {
		this.tagVoteDAO = tagVoteDAO;
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
		
		for (Tag tag : tagVoteDAO.getHandTagsForResource(resource)) {
			votes.add(new TaggingVote(tag, new HandTaggedVoter(), 100));
		}
		
		final boolean shouldAppearOnPublisherAndParentTagPages = 
		    resource.getType().equals("L") || resource.getType().equals("N")
		    || resource.getType().equals("C") || resource.getType().equals("F");
				
		if (shouldAppearOnPublisherAndParentTagPages) {            
		    
			addAncestorTagVotes(resource, votes);
		    			
		    if (((PublishedResource) resource).getPublisher() != null) {              
//		        for (Tag publisherTag : ((PublishedResource) resource).getPublisher().getTags()) {                
	//	            
		//            votes.add(new TaggingVote(publisherTag, new PublishersTagsVoter(), 100));		            		     		            
		  //          for (Tag publishersAncestor : publisherTag.getAncestors()) {
		    //				votes.add(new TaggingVote(publishersAncestor, new PublishersTagAncestorTagVoter(), 100));
		      // TODO     }
		       // }
		    }
		}
		return votes;
	}


	private void addAncestorTagVotes(Resource resource, List<TaggingVote> votes) {
		for (Tag tag : tagVoteDAO.getHandTagsForResource(resource)) {
			for (Tag ancestorTag: tag.getAncestors()) {
				votes.add(new TaggingVote(ancestorTag, new AncestorTagVoter(), 100));
			}
		}
	}

}
