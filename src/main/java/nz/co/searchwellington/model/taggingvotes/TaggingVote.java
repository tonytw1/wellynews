package nz.co.searchwellington.model.taggingvotes;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.taggingvotes.voters.TaggingVoter;

public interface TaggingVote {
	
	public Tag getTag();
	public TaggingVoter getVoter();
	
}
