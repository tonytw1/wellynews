package nz.co.searchwellington.model.taggingvotes;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.taggingvotes.voters.TaggingVoter;

public class TaggingVote {
	
	Tag tag;
	TaggingVoter voter;
	int weight;
		
	public TaggingVote(Tag tag, TaggingVoter voter, int weight) {
		this.tag = tag;
		this.voter = voter;
		this.weight = weight;
	}

	public Tag getTag() {
		return tag;
	}

	public TaggingVoter getVoter() {
		return voter;
	}

	public int getWeight() {
		return weight;
	}
	
}
