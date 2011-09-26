package nz.co.searchwellington.model.taggingvotes;

import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.taggingvotes.voters.TaggingVoter;

public class GeotaggingVote {

	Geocode geotag;
	TaggingVoter voter;
	int weight;

	public GeotaggingVote(Geocode geotag, TaggingVoter voter, int weight) {
		this.geotag = geotag;
		this.voter = voter;
		this.weight = weight;
	}

	public Geocode getGeotag() {
		return geotag;
	}
	
	public void setGeotag(Geocode geotag) {
		this.geotag = geotag;
	}

	public TaggingVoter getVoter() {
		return voter;
	}

	public void setVoter(TaggingVoter voter) {
		this.voter = voter;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
	
}
