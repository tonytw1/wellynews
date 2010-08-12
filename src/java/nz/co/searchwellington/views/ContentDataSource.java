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

}
