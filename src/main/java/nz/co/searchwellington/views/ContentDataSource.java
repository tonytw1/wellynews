package nz.co.searchwellington.views;

import java.util.List;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ContentRetrievalService;

public class ContentDataSource {

	private ContentRetrievalService contentRetrievalService;

	public ContentDataSource(ContentRetrievalService contentRetrievalService) {
		this.contentRetrievalService = contentRetrievalService;
	}

	public List<Resource> getLatestNewsitems() {
		return contentRetrievalService.getLatestNewsitems(5);
	}

	public List<Resource> getPublisherTagNewsitems(String publisherUrlWords, String tagName) {
		return contentRetrievalService.getPublisherTagCombinerNewsitems(publisherUrlWords, tagName, 10);
	}

}
