package nz.co.searchwellington.model.taggingvotes.voters;



public class FeedTagAncestorTagVoter implements TaggingVoter {

	@Override
	public String getVoterName() {
		return "Source feed's tag ancestors";
	}

}
