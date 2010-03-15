package nz.co.searchwellington.tagging;

public class FeedTagAncestorTagVoter implements TaggingVoter {

	@Override
	public String getVoterName() {
		return "Source feed's tag ancestors";
	}

}
