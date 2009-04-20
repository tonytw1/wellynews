package nz.co.searchwellington.controllers;

import nz.co.searchwellington.model.Website;

public class PublisherNewsitemCountService {

	// TODO make count showBroken conditional based on loggedInUserFilter.
	public int getNewsitemCount(Website publisher) {
		return publisher.getNewsitems().size();
	}
	
}
