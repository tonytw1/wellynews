package nz.co.searchwellington.controllers;

import java.util.List;

import nz.co.searchwellington.model.PublisherContentCount;
import nz.co.searchwellington.model.TagContentCount;

public class TagRelatedLinks {
	
	private List<TagContentCount> relatedTags;
	private List<PublisherContentCount> relatedPublisers;
	
	
	public List<TagContentCount> getRelatedTags() {
		return relatedTags;
	}
	public void setRelatedTags(List<TagContentCount> relatedTags) {
		this.relatedTags = relatedTags;
	}
	public List<PublisherContentCount> getRelatedPublisers() {
		return relatedPublisers;
	}
	public void setRelatedPublisers(List<PublisherContentCount> relatedPublisers) {
		this.relatedPublisers = relatedPublisers;
	}
	
	
	

}
