package nz.co.searchwellington.model.taggingvotes;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.taggingvotes.voters.TaggingVoter;

public class GeneratedTaggingVote implements TaggingVote {
	
	private Tag tag;
	private TaggingVoter voter;
	
	public GeneratedTaggingVote(Tag tag, TaggingVoter voter) {
		this.tag = tag;
		this.voter = voter;
	}

	@Override
	public Tag getTag() {
		return tag;
	}

	@Override
	public TaggingVoter getVoter() {
		return voter;
	}
	
}
